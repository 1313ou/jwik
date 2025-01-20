package edu.mit.jwi

import edu.mit.jwi.DeserializedRAMDictionary.IInputStreamFactory
import edu.mit.jwi.data.IHasLifecycle
import edu.mit.jwi.data.ILoadable
import edu.mit.jwi.data.LoadPolicy
import edu.mit.jwi.item.*
import edu.mit.jwi.item.Synset.Sense
import java.io.*
import java.net.URL
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.zip.GZIPOutputStream

/**
 * Dictionary that can be completely loaded into memory.
 * **Note:** If you receive an OutOfMemoryError while using this object, try increasing your heap size, by using the `-Xmx` switch.
 */
abstract class BaseRAMDictionary protected constructor(
) : IDictionary, ILoadable {

    internal val lifecycleLock: Lock = ReentrantLock()

    private val loadLock: Lock = ReentrantLock()

    @Volatile
    internal var state: IHasLifecycle.LifecycleState = IHasLifecycle.LifecycleState.CLOSED

    @Transient
    internal var loader: Thread? = null

    /**
     * Dictionary data
     */
    internal var data: DictionaryData? = null

    // L O A D

    override val isLoaded: Boolean
        get() = data != null

    override fun load() {
        try {
            load(false)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    @Throws(InterruptedException::class)
    override fun load(block: Boolean) {
        if (loader != null) {
            return
        }
        try {
            loadLock.lock()

            // if we are closed or in the process of closing, do nothing
            if (state == IHasLifecycle.LifecycleState.CLOSED || state == IHasLifecycle.LifecycleState.CLOSING) {
                return
            }

            if (loader != null) {
                return
            }
            loader = makeThread()
            loader!!.isDaemon = true
            loader!!.start()
            if (block) {
                loader!!.join()
            }
        } finally {
            loadLock.unlock()
        }
    }

    abstract fun startLoad(): Boolean

    abstract fun makeThread(): Thread

    // OPEN

    override val isOpen: Boolean
        get() {
            try {
                lifecycleLock.lock()
                return state == IHasLifecycle.LifecycleState.OPEN
            } finally {
                lifecycleLock.unlock()
            }
        }

    @Throws(IOException::class)
    override fun open(): Boolean {
        try {
            lifecycleLock.lock()

            // if the dictionary is already open, return true
            if (state == IHasLifecycle.LifecycleState.OPEN) {
                return true
            }

            // if the dictionary is not closed, return false
            if (state != IHasLifecycle.LifecycleState.CLOSED) {
                return false
            }

            // indicate the start of opening
            state = IHasLifecycle.LifecycleState.OPENING

            return startLoad()

        } finally {
            // make sure to clear the opening state
            state = assertLifecycleState()
            lifecycleLock.unlock()
        }
    }

    override fun close() {
        try {
            lifecycleLock.lock()

            // if we are already closed, do nothing
            if (state == IHasLifecycle.LifecycleState.CLOSED) {
                return
            }

            // if we are already closing, do nothing
            if (state != IHasLifecycle.LifecycleState.CLOSING) {
                return
            }

            state = IHasLifecycle.LifecycleState.CLOSING

            // stop loading first
            if (loader != null) {
                loader!!.interrupt()
                try {
                    loader!!.join()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                loader = null
            }

            // null out backing data
            data = null
        } finally {
            state = assertLifecycleState()
            lifecycleLock.unlock()
        }
    }

    /**
     * This is an internal utility method that determines whether this dictionary should be considered open or closed.
     *
     * @return the lifecycle state object representing open if the object is open; otherwise the lifecycle state object representing closed
     */
    private fun assertLifecycleState(): IHasLifecycle.LifecycleState {
        try {
            lifecycleLock.lock()

            // if the data object is present, then we are open
            if (data != null) {
                return IHasLifecycle.LifecycleState.OPEN
            }

            // otherwise we are closed
            return IHasLifecycle.LifecycleState.CLOSED
        } finally {
            lifecycleLock.unlock()
        }
    }

    // L O O K   U P

    // INDEX

    override fun getIndex(lemma: String, pos: POS): Index? {
        return getIndex(IndexID(lemma, pos))
    }

    override fun getIndex(id: IndexID): Index? {
        check(data != null) { NO_DATA }
        return data!!.indexes[id.pOS]!![id]
    }

    // SENSE

    override fun getSense(id: SenseID): Sense? {
        check(data != null) { NO_DATA }
        val resolver = data!!.synsets[id.pOS]!!
        val synset = resolver[id.synsetID]
        if (synset == null) {
            return null
        }
        return when (id) {
            is SenseIDWithNum   -> synset.senses[id.senseNumber - 1]
            is SenseIDWithLemma -> synset.senses.first { it.lemma.equals(id.lemma, ignoreCase = true) }
            else                -> throw IllegalArgumentException("Not enough information in SenseID instance to retrieve sense.")
        }
    }

    override fun getSense(sk: SenseKey): Sense? {
        check(data != null) { NO_DATA }
        return data!!.senses[sk.key]
    }

    override fun getLemmasStartingWith(start: String, pos: POS?, limit: Int): Set<String> {
        check(data != null) { NO_DATA }
        return data!!.senses.values
            .filter { it.lemma.startsWith(start) && if (pos != null) it.pOS == pos else true }
            .map { it.lemma }
            .take(limit)
            .toSet()
    }

    // SYNSET

    override fun getSynset(id: SynsetID): Synset? {
        check(data != null) { NO_DATA }
        return data!!.synsets[id.pOS]!![id]
    }

    // SENSE ENTRY

    override fun getSenseEntry(key: SenseKey): SenseEntry? {
        check(data != null) { NO_DATA }
        return data!!.senseEntries[key.sensekey]
    }

    // EXCEPTION ENTRY

    override fun getExceptionEntry(surfaceForm: String, pos: POS): ExceptionEntry? {
        return getExceptionEntry(ExceptionEntryID(surfaceForm, pos))
    }

    override fun getExceptionEntry(id: ExceptionEntryID): ExceptionEntry? {
        check(data != null) { NO_DATA }
        return data!!.exceptions[id.pOS]!![id]
    }

    // I T E R A T E

    override fun getIndexIterator(pos: POS): Iterator<Index> {
        check(data != null) { NO_DATA }
        return data!!.indexes[pos]!!.values.iterator()
    }

    override fun getSynsetIterator(pos: POS): Iterator<Synset> {
        check(data != null) { NO_DATA }
        return data!!.synsets[pos]!!.values.iterator()
    }

    override fun getSenseEntryIterator(): Iterator<SenseEntry> {
        check(data != null) { NO_DATA }
        return data!!.senseEntries.values.iterator()
    }

    override fun getExceptionEntryIterator(pos: POS): Iterator<ExceptionEntry> {
        check(data != null) { NO_DATA }
        return data!!.exceptions[pos]!!.values.iterator()
    }

    // E X P O R T

    /**
     * Exports the in-memory contents of the data to the specified output stream.
     * This method flushes and closes the output stream when it is done writing
     * the data.
     *
     * @param out the output stream to which the in-memory data will be written
     * @throws IOException           if there is a problem writing the in-memory data to the output stream.
     * @throws IllegalStateException if the dictionary has not been loaded into memory
     */
    @Throws(IOException::class)
    fun export(out: OutputStream) {
        try {
            loadLock.lock()
            check(isLoaded) { "RAMDictionary not loaded into memory" }

            GZIPOutputStream(out).use {
                BufferedOutputStream(it).use {
                    ObjectOutputStream(it).use {
                        it.writeObject(data)
                        it.flush()
                    }
                }
            }
        } finally {
            loadLock.unlock()
        }
    }

    /**
     * Object that holds all the dictionary data loaded from the Wordnet files.
     *
     * Constructs an empty dictionary data object.
     */
    data class DictionaryData(
        val version: Version?,
        val indexes: Map<POS, Map<IndexID, Index>>,
        val synsets: Map<POS, Map<SynsetID, Synset>>,
        val exceptions: Map<POS, Map<ExceptionEntryID, ExceptionEntry>>,
        val senses: Map<String, Sense>,
        val senseEntries: Map<String, SenseEntry>,
        ) : Serializable

    companion object {

        const val NO_DATA = "Data not loaded into memory"

        /**
         * This is a convenience method that transforms a Wordnet dictionary at the
         * specified file location into an in-memory image written to the specified
         * output stream. The file may point to either a directory or in-memory
         * image.
         *
         * @param in the file from which the Wordnet data should be loaded
         * @param out the output stream to which the Wordnet data should be written
         * @return true if the export was successful
         * @throws IOException          if there is an IO problem when opening or exporting the dictionary.
         */
        @Throws(IOException::class)
        fun export(`in`: File, out: OutputStream): Boolean {
            return export(RAMDictionary(`in`, LoadPolicy.IMMEDIATE_LOAD), out)
        }

        /**
         * This is a convenience method that transforms a Wordnet dictionary at the
         * specified url location into an in-memory image written to the specified
         * output stream. The url may point to either a directory or in-memory
         * image.
         *
         * @param in the url from which the Wordnet data should be loaded
         * @param out the output stream to which the Wordnet data should be written
         * @return true if the export was successful
         * @throws IOException          if there is an IO problem when opening or exporting the dictionary.
         */
        @Throws(IOException::class)
        fun export(`in`: URL, out: OutputStream): Boolean {
            return export(RAMDictionary(`in`, LoadPolicy.IMMEDIATE_LOAD), out)
        }

        /**
         * This is a convenience method that transforms a Wordnet dictionary drawn
         * from the specified input stream factory into an in-memory image written to
         * the specified output stream.
         *
         * @param in the file from which the Wordnet data should be loaded
         * @param out the output stream to which the Wordnet data should be written
         * @return true if the export was successful
         * @throws IOException          if there is an IO problem when opening or exporting the dictionary.
         */
        @Throws(IOException::class)
        fun export(`in`: IInputStreamFactory, out: OutputStream): Boolean {
            return export(DeserializedRAMDictionary(`in`), out)
        }

        /**
         * Exports a specified RAM Dictionary object to the specified output stream.
         * This is convenience method.
         *
         * @param dict the dictionary to be exported; the dictionary will be closed at the end of the method.
         * @param out the output stream to which the data will be written.
         * @return true if the export was successful
         * @throws IOException if there was a IO problem during export
         */
        @Throws(IOException::class)
        private fun export(dict: BaseRAMDictionary, out: OutputStream): Boolean {
            // load initial data into memory
            var dict = dict
            print("Performing load...")
            dict.open()
            println("(done)")

            // export to intermediate file
            print("Performing export...")
            dict.export(out)
            dict.close()
            //dict = null
            System.gc()
            println("(done)")
            return true
        }
    }
}