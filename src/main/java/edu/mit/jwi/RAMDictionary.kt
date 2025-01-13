/* ******************************************************************************
 * Java Wordnet Interface Library (JWI) v2.4.0
 * Copyright (c) 2007-2015 Mark A. Finlayson
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 4.0
 * International Public License, which means it may be freely used for all
 * purposes, as long as proper acknowledgment is made.  See the license file
 * included with this distribution for more details.
 *******************************************************************************/
package edu.mit.jwi

import edu.mit.jwi.data.FileProvider
import edu.mit.jwi.data.IHasLifecycle.LifecycleState
import edu.mit.jwi.data.IHasLifecycle.ObjectOpenException
import edu.mit.jwi.data.ILoadable
import edu.mit.jwi.data.LoadPolicy.BACKGROUND_LOAD
import edu.mit.jwi.data.LoadPolicy.IMMEDIATE_LOAD
import edu.mit.jwi.item.*
import edu.mit.jwi.item.Synset.IWordBuilder
import java.io.*
import java.net.URL
import java.util.concurrent.Callable
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.Throws

class RAMDictionary(
    /**
     * The dictionary that backs this instance; may be null. if factory is not null
     */
    val backingDictionary: IDictionary,
    loadPolicy: Int,
    config: Config? = null,
) : BaseRAMDictionary(config) {

    var loadPolicy: Int = loadPolicy
        set(policy) {
            if (isOpen)
                throw ObjectOpenException()
            loadPolicy = policy
        }

    override fun configure(config: Config?) {
        backingDictionary.configure(config)
    }

    /**
     * Loads data from the specified File using the specified load policy.
     *
     * Constructs a new wrapper RAM dictionary that will load the contents the specified local Wordnet data, with the specified load policy.
     *
     * @param file a file pointing to a local copy of wordnet
     * @param loadPolicy the load policy of the dictionary
     * @param config config bundle
     */
    @JvmOverloads
    constructor(
        file: File,
        loadPolicy: Int = DEFAULT_LOAD_POLICY,
        config: Config? = null,
    ) : this(createBackingDictionary(file)!!, loadPolicy, config)

    /**
     * Loads data from the specified URL using the specified load policy.
     *
     * Constructs a new RAMDictionary that will load the contents the specified Wordnet data using the default load policy.
     *
     * @param url an url pointing to a local copy of wordnet; may not be null
     * @param loadPolicy the load policy of the dictionary
     * @param config config bundle
     */
    @JvmOverloads
    constructor(
        url: URL,
        loadPolicy: Int = DEFAULT_LOAD_POLICY,
        config: Config? = null,
    ) : this(createBackingDictionary(url)!!, loadPolicy, config)

    override fun startLoad(): Boolean {

        // behavior when loading from a backing dictionary depends on the load policy
        val result = backingDictionary.open()
        if (result) {
            try {
                when (loadPolicy) {
                    IMMEDIATE_LOAD  -> load(true)
                    BACKGROUND_LOAD -> load(false)
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
                return false
            }
        }
        return result
    }

    /**
     * This runnable loads the dictionary data into memory and sets the appropriate variable in the parent dictionary.
     */
    private inner class JWIBackgroundDataLoader : Runnable {

        override fun run() {
            try {
                // we have a backing dictionary from which we should load our data
                val loader = DataLoader(backingDictionary)
                data = loader.call()
                backingDictionary.close()
            } catch (t: Throwable) {
                if (!Thread.currentThread().isInterrupted) {
                    t.printStackTrace()
                    System.err.println("Unable to load dictionary data into memory")
                }
            }
        }
    }

    override fun makeThread(): Thread {
        val t = Thread(JWIBackgroundDataLoader())
        t.setName(JWIBackgroundDataLoader::class.java.getSimpleName())
        return t
    }

    /**
     * This is an internal utility method that determines whether this
     * dictionary should be considered open or closed.
     *
     * @return the lifecycle state object representing open if the object is
     * open; otherwise the lifecycle state object representing closed
     */
    private fun assertLifecycleState(): LifecycleState {
        try {
            lifecycleLock.lock()

            // if the data object is present, then we are open
            if (data != null) {
                return LifecycleState.OPEN
            }

            // if the backing dictionary is present and open, then we are open
            if (backingDictionary.isOpen) {
                return LifecycleState.OPEN
            }

            // otherwise we are closed
            return LifecycleState.CLOSED
        } finally {
            lifecycleLock.unlock()
        }
    }

    override fun close() {
        try {
            lifecycleLock.lock()

            // if we are already closed, do nothing
            if (state == LifecycleState.CLOSED) {
                return
            }

            // if we are already closing, do nothing
            if (state != LifecycleState.CLOSING) {
                return
            }

            state = LifecycleState.CLOSING

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

            // next close backing dictionary if it exists
            backingDictionary.close()

            // null out backing data
            data = null
        } finally {
            state = assertLifecycleState()
            lifecycleLock.unlock()
        }
    }

    override fun getIndexWord(id: IndexWordID): IndexWord? {
        return if (data != null) super.getIndexWord(id) else return backingDictionary.getIndexWord(id)
    }

    override fun getWord(id: IWordID): Word? {
        return if (data != null) super.getWord(id) else backingDictionary.getWord(id)
    }

    override fun getWord(key: SenseKey): Word? {
        return if (data != null) super.getWord(key) else backingDictionary.getWord(key)
    }

    override fun getWords(start: String, pos: POS?, limit: Int): Set<String> {
        return if (data != null) super.getWords(start, pos, limit) else backingDictionary.getWords(start, pos, limit)
    }

    override fun getSynset(id: SynsetID): Synset? {
        return if (data != null) super.getSynset(id) else return backingDictionary.getSynset(id)
    }

    override fun getSenseEntry(key: SenseKey): SenseEntry? {
        return if (data != null) super.getSenseEntry(key) else backingDictionary.getSenseEntry(key)
    }

    override fun getExceptionEntry(id: ExceptionEntryID): ExceptionEntry? {
        return if (data != null) super.getExceptionEntry(id) else backingDictionary.getExceptionEntry(id)
    }

    override val version: Version?
        get() {
            return backingDictionary.version
        }

    override fun getIndexWordIterator(pos: POS): Iterator<IndexWord> {
        return HotSwappableIndexWordIterator(pos)
    }

    override fun getSynsetIterator(pos: POS): Iterator<Synset> {
        return HotSwappableSynsetIterator(pos)
    }

    override fun getSenseEntryIterator(): Iterator<SenseEntry> {
        return HotSwappableSenseEntryIterator()
    }

    override fun getExceptionEntryIterator(pos: POS): Iterator<ExceptionEntry> {
        return HotSwappableExceptionEntryIterator(pos)
    }

    /**
     * An iterator that allows the dictionary to be loaded into memory while it is iterating.
     *
     * Constructs a new hot swappable iterator.
     *
     * @param itr the wrapped iterator
     * @param <E> the element type of the iterator
     * @param checkForLoad if true, on each call the iterator checks to see if the dictionary has been loaded into memory, switching data sources if so
     */
    private abstract inner class HotSwappableIterator<E>(
        private var itr: Iterator<E>,
        private var checkForLoad: Boolean,
    ) : Iterator<E> {

        private var last: E? = null

        override fun hasNext(): Boolean {
            if (checkForLoad) {
                checkForLoad()
            }
            return itr.hasNext()
        }

        override fun next(): E {
            if (checkForLoad) {
                checkForLoad()
                last = itr.next()
                return last!!
            } else {
                return itr.next()
            }
        }

        /**
         * Checks to see if the data has been loaded into memory; is so,
         * replaces the original iterator with one that iterates over the
         * in-memory data structures.
         */
        fun checkForLoad() {
            if (data == null) {
                return
            }
            checkForLoad = false
            itr = makeIterator()
            if (last != null) {
                while (itr.hasNext()) {
                    val consume: E? = itr.next()
                    if (last == consume) {
                        return
                    }
                }
                throw IllegalStateException()
            }
        }

        /**
         * Constructs the iterator that will iterate over the loaded data.
         *
         * @return the new iterator to be swapped in when loading is done
         */
        abstract fun makeIterator(): Iterator<E>
    }

    /**
     * A hot swappable iterator for index words.
     *
     * @param pos the part of speech for the iterator
     */
    private inner class HotSwappableIndexWordIterator(private val pos: POS) :
        HotSwappableIterator<IndexWord>(
            if (data == null) backingDictionary.getIndexWordIterator(pos) else data!!.idxWords[pos]!!.values.iterator(),
            data == null
        ) {

        override fun makeIterator(): Iterator<IndexWord> {
            val m = data!!.idxWords[pos]!!
            return m.values.iterator()
        }
    }

    /**
     * A hot swappable iterator for synsets.
     *
     * @param pos the part of speech for the iterator
     */
    private inner class HotSwappableSynsetIterator(private val pos: POS) :
        HotSwappableIterator<Synset>(
            if (data == null) backingDictionary.getSynsetIterator(pos) else data!!.synsets[pos]!!.values.iterator(),
            data == null
        ) {

        override fun makeIterator(): Iterator<Synset> {
            val m = data!!.synsets[pos]!!
            return m.values.iterator()
        }
    }

    /**
     * A hot swappable iterator that iterates over exceptions entries for a particular part of speech.
     *
     * @param pos the part of speech for this iterator, may not be null
     */
    private inner class HotSwappableExceptionEntryIterator(private val pos: POS) :
        HotSwappableIterator<ExceptionEntry>(
            if (data == null) backingDictionary.getExceptionEntryIterator(pos) else data!!.exceptions[pos]!!.values.iterator(),
            data == null
        ) {

        override fun makeIterator(): Iterator<ExceptionEntry> {
            return data!!.exceptions[pos]!!.values.iterator()
        }
    }

    /**
     * A hot swappable iterator that iterates over sense entries.
     */
    private inner class HotSwappableSenseEntryIterator :
        HotSwappableIterator<SenseEntry>(
            if (data == null) backingDictionary.getSenseEntryIterator() else data!!.senses.values.iterator(),
            data == null
        ) {

        override fun makeIterator(): Iterator<SenseEntry> {
            return data!!.senses.values.iterator()
        }
    }

    companion object {

        /**
         * Creates a [DataSourceDictionary] out of the specified file, as long
         * as the file points to an existing local directory.
         *
         * @param file the local directory for which to create a data source dictionary
         * @return a dictionary object that uses the specified local directory as its data source; otherwise, null
         */
        fun createBackingDictionary(file: File): IDictionary? {
            return if (FileProvider.isLocalDirectory(file)) DataSourceDictionary(FileProvider(file)) else null
        }

        /**
         * Creates a [DataSourceDictionary] out of the specified url, as long
         * as the url points to an existing local directory.
         *
         * @param url the local directory for which to create a data source dictionary
         * @return a dictionary object that uses the specified local directory as its data source; otherwise, null
         */
        fun createBackingDictionary(url: URL): IDictionary? {
            return if (FileProvider.isLocalDirectory(url)) DataSourceDictionary(FileProvider(url)) else null
        }
    }
}

class RAMSerDictionary(
    /**
     * The stream factory that backs this instance
     */
    val streamFactory: IInputStreamFactory,
    config: Config? = null,
) : BaseRAMDictionary(config) {

    var loadPolicy: Int = IMMEDIATE_LOAD
        set(_) {
            if (isOpen)
                throw ObjectOpenException()
            // if the dictionary uses an input stream factory the load policy is effectively IMMEDIATE_LOAD so the load policy is set to this for information purposes
            loadPolicy = IMMEDIATE_LOAD
        }

    /**
     * Loads data from the specified File using the specified load policy.
     *
     * Constructs a new wrapper RAM dictionary that will load the contents the specified local Wordnet data, with the specified load policy.
     *
     * @param file a file pointing to a local copy of wordnet
     * @param config config bundle
     */
    constructor(
        file: File,
        config: Config? = null,

        ) : this(createInputStreamFactory(file)!!, config)

    /**
     * Loads data from the specified URL using the specified load policy.
     *
     * Constructs a new RAMDictionary that will load the contents the specified Wordnet data using the default load policy.
     *
     * @param url an url pointing to a local copy of wordnet; may not be null
     * @param config config bundle
     */
    constructor(
        url: URL,
        config: Config? = null,

        ) : this(createInputStreamFactory(url)!!, config)

    override fun configure(config: Config?) {
        streamFactory.configure(config)
    }

    override fun startLoad(): Boolean {

        // behavior when loading from an input stream is immediate load
        try {
            load(true)
        } catch (e: InterruptedException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    override fun makeThread(): Thread {
        val t = Thread(JWIBackgroundDataLoader())
        t.setName(JWIBackgroundDataLoader::class.java.getSimpleName())
        return t
    }

    /**
     * This runnable loads the dictionary data into memory and sets the appropriate variable in the parent dictionary.
     */
    private inner class JWIBackgroundDataLoader : Runnable {

        override fun run() {
            try {
                // read the dictionary data from the stream factory
                streamFactory.makeInputStream().use {
                    GZIPInputStream(it).use {
                        BufferedInputStream(it).use {
                            ObjectInputStream(it).use {
                                data = it.readObject() as DictionaryData?
                            }
                        }
                    }
                }
            } catch (t: Throwable) {
                if (!Thread.currentThread().isInterrupted) {
                    t.printStackTrace()
                    System.err.println("Unable to load dictionary data into memory")
                }
            }
        }
    }

    override val version: Version?
        get() {
            if (data != null) {
                return data!!.version
            }
            return null
        }

    companion object {

        /**
         * Creates an input stream factory out of the specified File. If the file
         * points to a local directory then the method returns null.
         *
         * @param file the file out of which to make an input stream factory
         * @return a new input stream factory, or null if the url points to a local directory.
         */
        fun createInputStreamFactory(file: File): IInputStreamFactory? {
            return if (FileProvider.isLocalDirectory(file)) null else FileInputStreamFactory(file)
        }

        /**
         * Creates an input stream factory out of the specified URL. If the url
         * points to a local directory then the method returns null.
         *
         * @param url the url out of which to make an input stream factory
         * @return a new input stream factory, or null if the url points to a local directory.
         */
        fun createInputStreamFactory(url: URL): IInputStreamFactory? {
            return if (FileProvider.isLocalDirectory(url)) null else URLInputStreamFactory(url)
        }
    }
}

/**
 * Dictionary that can be completely loaded into memory.

 * Designed to wrap an arbitrary dictionary object.
 * **Note:** If you receive an [OutOfMemoryError] while using this object, try increasing your heap size, by using the `-Xmx` switch.
 *
 * @param config config bundle
 */
abstract class BaseRAMDictionary protected constructor(
    config: Config? = null,
) : IDictionary, ILoadable {

    internal val lifecycleLock: Lock = ReentrantLock()

    private val loadLock: Lock = ReentrantLock()

    @Volatile
    internal var state: LifecycleState = LifecycleState.CLOSED

    @Transient
    internal var loader: Thread? = null

    internal var data: DictionaryData? = null

    /**
     * Unifies the constructor decision matrix.
     * Exactly one of the backing dictionary or the input factory must be non-null, otherwise an exception is thrown.
     * If the factory is non-null, the dictionary will ignore the specified load policy and set the load policy to "immediate load".
     */
    init {
        configure(config)
    }

    // LOAD

    override val isLoaded: Boolean
        get() = data != null

    override fun load() {
        try {
            load(false)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    abstract fun makeThread(): Thread

    @Throws(InterruptedException::class)
    override fun load(block: Boolean) {
        if (loader != null) {
            return
        }
        try {
            loadLock.lock()

            // if we are closed or in the process of closing, do nothing
            if (state == LifecycleState.CLOSED || state == LifecycleState.CLOSING) {
                return
            }

            if (loader != null) {
                return
            }
            loader = makeThread()
            loader!!.setDaemon(true)
            loader!!.start()
            if (block) {
                loader!!.join()
            }
        } finally {
            loadLock.unlock()
        }
    }

    // OPEN

    override val isOpen: Boolean
        get() {
            try {
                lifecycleLock.lock()
                return state == LifecycleState.OPEN
            } finally {
                lifecycleLock.unlock()
            }
        }

    abstract fun startLoad(): Boolean

    @Throws(IOException::class)
    override fun open(): Boolean {
        try {
            lifecycleLock.lock()

            // if the dictionary is already open, return true
            if (state == LifecycleState.OPEN) {
                return true
            }

            // if the dictionary is not closed, return false;
            if (state != LifecycleState.CLOSED) {
                return false
            }

            // indicate the start of opening
            state = LifecycleState.OPENING

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
            if (state == LifecycleState.CLOSED) {
                return
            }

            // if we are already closing, do nothing
            if (state != LifecycleState.CLOSING) {
                return
            }

            state = LifecycleState.CLOSING

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
     * This is an internal utility method that determines whether this
     * dictionary should be considered open or closed.
     *
     * @return the lifecycle state object representing open if the object is
     * open; otherwise the lifecycle state object representing closed
     */
    private fun assertLifecycleState(): LifecycleState {
        try {
            lifecycleLock.lock()

            // if the data object is present, then we are open
            if (data != null) {
                return LifecycleState.OPEN
            }

            // otherwise we are closed
            return LifecycleState.CLOSED
        } finally {
            lifecycleLock.unlock()
        }
    }

    // EXPORT

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

    // F I N D

    // INDEXWORD

    override fun getIndexWord(lemma: String, pos: POS): IndexWord? {
        return getIndexWord(IndexWordID(lemma, pos))
    }

    override fun getIndexWord(id: IndexWordID): IndexWord? {
        return if (data != null) data!!.idxWords[id.pOS]!![id] else null
    }

    // WORD

    override fun getWord(id: IWordID): Word? {
        if (data != null) {
            val resolver = data!!.synsets[id.pOS]!!
            val synset = resolver[id.synsetID]
            if (synset == null) {
                return null
            }
            return when (id) {
                is WordNumID   -> synset.words[id.wordNumber - 1]
                is WordLemmaID -> synset.words.first { it.lemma.equals(id.lemma, ignoreCase = true) }
                else           -> throw IllegalArgumentException("Not enough information in IWordID instance to retrieve word.")
            }
        } else return null
    }

    override fun getWord(key: SenseKey): Word? {
        return if (data != null) data!!.words[key] else null
    }

    override fun getWords(start: String, pos: POS?, limit: Int): Set<String> {
        return if (data != null) {
            var count = 0
            data!!.words.values
                .filter { it.lemma.startsWith(start) && if (pos != null) it.pOS == pos else true }
                .also { count++ }
                .map { it.lemma }
                .takeUnless { count > limit }
                ?.toSet() ?: emptySet()
        } else emptySet()
    }

    // SYNSET

    override fun getSynset(id: SynsetID): Synset? {
        return if (data != null) data!!.synsets[id.pOS]!![id] else null
    }

    // SENSE ENTRY

    override fun getSenseEntry(key: SenseKey): SenseEntry? {
        return if (data != null) data!!.senses[key] else null
    }

    // EXCEPTION ENTRY

    override fun getExceptionEntry(surfaceForm: String, pos: POS): ExceptionEntry? {
        return getExceptionEntry(ExceptionEntryID(surfaceForm, pos))
    }

    override fun getExceptionEntry(id: ExceptionEntryID): ExceptionEntry? {
        return if (data != null) data!!.exceptions[id.pOS]!![id] else null
    }

    // ITERATORS

    override fun getIndexWordIterator(pos: POS): Iterator<IndexWord> {
        check(data == null) { "Data not loaded into memory" }
        return data!!.idxWords[pos]!!.values.iterator()
    }

    override fun getSynsetIterator(pos: POS): Iterator<Synset> {
        check(data == null) { "Data not loaded into memory" }
        return data!!.synsets[pos]!!.values.iterator()
    }

    override fun getSenseEntryIterator(): Iterator<SenseEntry> {
        check(data == null) { "Data not loaded into memory" }
        return data!!.senses.values.iterator()
    }

    override fun getExceptionEntryIterator(pos: POS): Iterator<ExceptionEntry> {
        check(data == null) { "Data not loaded into memory" }
        return data!!.exceptions[pos]!!.values.iterator()
    }

    /**
     * A `Callable` that creates a dictionary data from a specified
     * dictionary. The data loader does not change the open state of the
     * dictionary; the dictionary for the loader must be open for the loader to
     * function without throwing an exception. The loader may be called multiple
     * times (in a thread-safe manner) as long as the dictionary is open.
     *
     * Constructs a new data loader object, that uses the specified
     * dictionary to load its data.
     *
     * @param source source dictionary
     */
    class DataLoader(private val source: IDictionary) : Callable<DictionaryData?> {

        override fun call(): DictionaryData? {
            val result = DictionaryData()
            result.version = source.version

            val t = Thread.currentThread()

            for (pos in POS.entries) {
                // index words
                var idxWords = result.idxWords[pos]!!
                run {
                    val i: Iterator<IndexWord> = source.getIndexWordIterator(pos)
                    while (i.hasNext()) {
                        val idxWord = i.next()
                        idxWords.put(idxWord.iD, idxWord)
                    }
                }
                if (t.isInterrupted) {
                    return null
                }

                // synsets and words
                var synsets = result.synsets[pos]!!
                run {
                    val i: Iterator<Synset> = source.getSynsetIterator(pos)
                    while (i.hasNext()) {
                        val synset = i.next()
                        synsets.put(synset.iD, synset)
                        for (word in synset.words) {
                            result.words.put(word.senseKey, word)
                        }
                    }
                }
                if (t.isInterrupted) {
                    return null
                }

                // exceptions
                var exceptions = result.exceptions[pos]!!
                val i: Iterator<ExceptionEntry> = source.getExceptionEntryIterator(pos)
                while (i.hasNext()) {
                    val exception = i.next()
                    exceptions.put(exception.iD, exception)
                }
                if (t.isInterrupted) {
                    return null
                }
            }

            // sense entries
            val i: Iterator<SenseEntry> = source.getSenseEntryIterator()
            while (i.hasNext()) {
                val entry = i.next()
                val word: Word = result.words[entry.senseKey]!!
                result.senses.put(word.senseKey, makeSenseEntry(word.senseKey, entry))
            }
            if (t.isInterrupted) {
                return null
            }

            result.compactSize()
            if (t.isInterrupted) {
                return null
            }

            result.compactObjects()
            if (t.isInterrupted) {
                return null
            }
            return result
        }

        /**
         * Creates a new sense entry that replicates the specified sense entry.
         * The new sense entry replaces its internal sense key with the
         * specified sense key thus removing a redundant object.
         *
         * @param key the sense key to be used
         * @param old the sense entry to be replicated
         * @return the new sense entry object
         */
        private fun makeSenseEntry(key: SenseKey, old: SenseEntry): SenseEntry {
            return SenseEntry(key, old.offset, old.senseNumber, old.tagCount)
        }
    }

    /**
     * Object that holds all the dictionary data loaded from the Wordnet files.
     *
     * Constructs an empty dictionary data object.
     */
    class DictionaryData : Serializable {

        var version: Version? = null

        val idxWords: MutableMap<POS, MutableMap<IndexWordID, IndexWord>> = makePOSMap<IndexWordID, IndexWord>()

        val synsets: MutableMap<POS, MutableMap<SynsetID, Synset>> = makePOSMap<SynsetID, Synset>()

        val exceptions: MutableMap<POS, MutableMap<ExceptionEntryID, ExceptionEntry>> = makePOSMap<ExceptionEntryID, ExceptionEntry>()

        var words: MutableMap<SenseKey, Word> = makeMap<SenseKey, Word>(212500, null)

        var senses: MutableMap<SenseKey, SenseEntry> = makeMap<SenseKey, SenseEntry>(212500, null)

        /**
         * This method is used when constructing the dictionary data object.
         * Constructs a map with an empty sub-map for every part of speech.
         * Subclasses may override to change map character
         *
         * @param <K> the type of the keys for the sub-maps
         * @param <V> the type of the values for the sub-maps
         * @return a map with an empty sub-map for every part of speech.
         */
        private fun <K, V> makePOSMap(): MutableMap<POS, MutableMap<K, V>> {
            val result: MutableMap<POS, MutableMap<K, V>> = HashMap<POS, MutableMap<K, V>>(POS.entries.size)
            for (pos in POS.entries) {
                result.put(pos, makeMap<K, V>(4096, null))
            }
            return result
        }

        /**
         * Creates the actual sub-maps for the part-of-speech maps. This
         * particular implementation creates `LinkedHashMap` maps.
         *
         * @param <K>         the type of the keys for the sub-maps
         * @param <V>         the type of the values for the sub-maps
         * @param initialSize the initial size of the map; this parameter is ignored if the `contents` parameter is non-null.
         * @param contents    the items to be inserted in the map, may be null. If non-null, the `initialSize` parameter is ignored
         * @return an empty map with either the specified initial size, or contained the specified contents
         * @throws IllegalArgumentException if the initial size is invalid (less than 1) and the specified contents are null
         */
        private fun <K, V> makeMap(initialSize: Int, contents: MutableMap<K, V>?): MutableMap<K, V> {
            return if (contents == null) LinkedHashMap<K, V>(initialSize) else LinkedHashMap<K, V>(contents)
        }

        /**
         * Compacts this dictionary data object by resizing the internal maps, and removing redundant objects where possible.
         */
        fun compact() {
            compactSize()
            compactObjects()
        }

        /**
         * Resizes the internal data maps to be the exact size to contain their data.
         */
        fun compactSize() {
            compactPOSMap<IndexWordID, IndexWord>(idxWords)
            compactPOSMap<SynsetID, Synset>(synsets)
            compactPOSMap<ExceptionEntryID, ExceptionEntry>(exceptions)
            words = compactMap<SenseKey, Word>(words)
            senses = compactMap<SenseKey, SenseEntry>(senses)
        }

        /**
         * Compacts a part-of-speech map
         *
         * @param map the part-of-speech keyed map to be compacted
         * @param <K> key type
         * @param <V> value type
         */
        private fun <K, V> compactPOSMap(map: MutableMap<POS, MutableMap<K, V>>) {
            for (entry in map.entries) {
                entry.setValue(compactMap<K, V>(entry.value))
            }
        }

        /**
         * Compacts a regular map.
         *
         * @param map the map to be compacted
         * @param <K> key type
         * @param <V> value type
         * @return the new, compacted map
         */
        private fun <K, V> compactMap(map: MutableMap<K, V>): MutableMap<K, V> {
            return makeMap<K, V>(-1, map)
        }

        /**
         * Replaces redundant objects where possible
         */
        fun compactObjects() {
            for (pos in POS.entries) {
                val synsetMap = synsets[pos]!!
                for (entry in synsetMap.entries) {
                    entry.setValue(makeSynset(entry.value))
                }
                val indexMap = idxWords[pos]!!
                for (entry in indexMap.entries) {
                    entry.setValue(makeIndexWord(entry.value))
                }
            }
        }

        /**
         * Creates a new synset object that replaces all the old internal `ISynsetID` objects with those from the denoted synsets,
         * thus throwing away redundant synset ids.
         *
         * @param old the synset to be replicated
         * @return the new synset, a copy of the first
         */
        private fun makeSynset(old: Synset): Synset {

            // words
            val wordBuilders = old.words
                .map { WordBuilder(it) }
                .toList()

            // related synsets
            val newRelated = old.related
                .map { (ptr, oldTargets) ->
                    val newTargets: List<SynsetID> = oldTargets
                        .map {
                            val resolver: Map<SynsetID, Synset> = synsets[it.pOS]!!
                            val otherSynset: Synset = resolver[it]!!
                            otherSynset.iD
                        }
                        .toList()
                    ptr to newTargets
                }
                .toMap()

            return Synset(old.iD, old.lexicalFile, old.isAdjectiveSatellite, old.isAdjectiveHead, old.gloss, wordBuilders, newRelated)
        }

        /**
         * Creates a new word object that replaces all the old internal `IWordID` objects with those from the denoted words, thus throwing away redundant word ids.
         *
         * @param newSynset the synset for which the word is being made
         * @param old       the word to be replicated
         * @return the new synset, a copy of the first
         */
        private fun makeWord(newSynset: Synset, old: Word): Word {

            // related words
            val newRelated = old.related
                .map { (ptr, oldTargets) ->
                    val newTargets: List<IWordID> = oldTargets
                        .map { it as WordNumID }
                        .map {
                            val resolver: Map<SynsetID, Synset> = synsets[it.pOS]!!
                            val otherSynset: Synset = resolver[it.synsetID]!!
                            otherSynset.words[it.wordNumber - 1].iD
                        }
                        .toList()
                    ptr to newTargets
                }
                .toMap()

            // word
            val word = Word(newSynset, old.iD, old.lexicalID, old.adjectiveMarker, old.verbFrames, newRelated)
            if (word.senseKey.needsHeadSet()) {
                val oldKey = old.senseKey
                word.senseKey.setHead(oldKey.headWord!!, oldKey.headID)
            }
            return word
        }

        /**
         * Creates a new index word that replicates the specified index word.
         * The new index word replaces its internal synset ids with synset ids
         * from the denoted synsets, thus removing redundant ids.
         *
         * @param old the index word to be replicated
         * @return the new index word object
         */
        private fun makeIndexWord(old: IndexWord): IndexWord {
            val newIDs: Array<IWordID> = Array(old.wordIDs.size) { i ->
                var oldID: IWordID = old.wordIDs[i]
                val resolver = synsets[oldID.pOS]!!
                var synset: Synset = resolver[oldID.synsetID]!!
                val newWord = synset.words.first { it.iD == oldID }
                newWord.iD
            }
            return IndexWord(old.iD, old.tagSenseCount, newIDs)
        }

        /**
         * A utility class that allows us to build word objects
         *
         * Constructs a new word builder object out of the specified old
         * synset and word.
         *
         * @param oldWord   the old word that backs this builder
         */
        inner class WordBuilder(private val oldWord: Word) : IWordBuilder {

            override fun toWord(synset: Synset): Word {
                return makeWord(synset, oldWord)
            }
        }
    }

    companion object {

        /**
         * The default load policy of a [RAMDictionary] is to load data in the background when opened.
         */
        const val DEFAULT_LOAD_POLICY: Int = BACKGROUND_LOAD

        /**
         * This is a convenience method that transforms a Wordnet dictionary at the
         * specified file location into an in-memory image written to the specified
         * output stream. The file may point to either a directory or in-memory
         * image.
         *
         * @param in  the file from which the Wordnet data should be loaded
         * @param out the output stream to which the Wordnet data should be written
         * @return true if the export was successful
         * @throws IOException          if there is an IO problem when opening or exporting the dictionary.
         */
        @Throws(IOException::class)
        fun export(`in`: File, out: OutputStream): Boolean {
            return export(RAMDictionary(`in`, IMMEDIATE_LOAD), out)
        }

        /**
         * This is a convenience method that transforms a Wordnet dictionary at the
         * specified url location into an in-memory image written to the specified
         * output stream. The url may point to either a directory or in-memory
         * image.
         *
         * @param in  the url from which the Wordnet data should be loaded
         * @param out the output stream to which the Wordnet data should be written
         * @return true if the export was successful
         * @throws IOException          if there is an IO problem when opening or exporting the dictionary.
         */
        @Throws(IOException::class)
        fun export(`in`: URL, out: OutputStream): Boolean {
            return export(RAMDictionary(`in`, IMMEDIATE_LOAD), out)
        }

        /**
         * This is a convenience method that transforms a Wordnet dictionary drawn
         * from the specified input stream factory into an in-memory image written to
         * the specified output stream.
         *
         * @param in  the file from which the Wordnet data should be loaded
         * @param out the output stream to which the Wordnet data should be written
         * @return true if the export was successful
         * @throws IOException          if there is an IO problem when opening or exporting the dictionary.
         */
        @Throws(IOException::class)
        fun export(`in`: IInputStreamFactory, out: OutputStream): Boolean {
            return export(RAMSerDictionary(`in`), out)
        }

        /**
         * Exports a specified RAM Dictionary object to the specified output stream.
         * This is convenience method.
         *
         * @param dict the dictionary to be exported; the dictionary will be closed at the end of the method.
         * @param out  the output stream to which the data will be written.
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

    interface IInputStreamFactory {

        /**
         * Returns a new input stream from this factory.
         *
         * @return a new, unused input stream from this factory.
         * @throws IOException io exception
         */
        @Throws(IOException::class)
        fun makeInputStream(): InputStream

        fun configure(config: Config?)
    }

    /**
     * Default implementation of the [IInputStreamFactory] interface which creates an input stream from a specified File object.
     *
     * Creates a FileInputStreamFactory that uses the specified file.
     *
     * @param file the file from which the input streams should be created;
     */
    class FileInputStreamFactory(private val file: File) : IInputStreamFactory {

        @Throws(IOException::class)
        override fun makeInputStream(): InputStream {
            return FileInputStream(file)
        }

        override fun configure(config: Config?) {
            TODO("Not yet implemented")
        }
    }

    /**
     * Default implementation of the [IInputStreamFactory] interface which creates an input stream from a specified URL.
     *
     * Creates a URLInputStreamFactory that uses the specified url.
     *
     * @param url the url from which the input streams should be created;
     */
    class URLInputStreamFactory(val url: URL) : IInputStreamFactory {

        @Throws(IOException::class)
        override fun makeInputStream(): InputStream {
            return url.openStream()
        }

        override fun configure(config: Config?) {
            TODO("Not yet implemented")
        }
    }
}
