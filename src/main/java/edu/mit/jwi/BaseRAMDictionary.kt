package edu.mit.jwi

import edu.mit.jwi.DeserializedRAMDictionary.IInputStreamFactory
import edu.mit.jwi.data.IHasLifecycle
import edu.mit.jwi.data.ILoadable
import edu.mit.jwi.data.LoadPolicy
import edu.mit.jwi.item.*
import java.io.*
import java.net.URL
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.zip.GZIPOutputStream
import kotlin.Throws

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

    // INDEX WORD

    override fun getIndexWord(lemma: String, pos: POS): SenseIndex? {
        return getIndexWord(SenseIndexID(lemma, pos))
    }

    override fun getIndexWord(id: SenseIndexID): SenseIndex? {
        check(data != null) { NO_DATA }
        return data!!.idxWords[id.pOS]!![id]
    }

    // WORD

    override fun getSense(id: ISenseID): Sense? {
        check(data != null) { NO_DATA }
        val resolver = data!!.synsets[id.pOS]!!
        val synset = resolver[id.synsetID]
        if (synset == null) {
            return null
        }
        return when (id) {
            is SenseIDWithNum   -> synset.words[id.senseNumber - 1]
            is SenseIDWithLemma -> synset.words.first { it.lemma.equals(id.lemma, ignoreCase = true) }
            else                -> throw IllegalArgumentException("Not enough information in IWordID instance to retrieve word.")
        }
    }

    override fun getSense(key: SenseKey): Sense? {
        check(data != null) { NO_DATA }
        return data!!.words[key]
    }

    override fun getLemmasStartingWith(start: String, pos: POS?, limit: Int): Set<String> {
        check(data != null) { NO_DATA }
        return data!!.words.values
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
        return data!!.senses[key]
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

    override fun getIndexWordIterator(pos: POS): Iterator<SenseIndex> {
        check(data != null) { NO_DATA }
        return data!!.idxWords[pos]!!.values.iterator()
    }

    override fun getSynsetIterator(pos: POS): Iterator<Synset> {
        check(data != null) { NO_DATA }
        return data!!.synsets[pos]!!.values.iterator()
    }

    override fun getSenseEntryIterator(): Iterator<SenseEntry> {
        check(data != null) { NO_DATA }
        return data!!.senses.values.iterator()
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
    class DictionaryData : Serializable {

        var version: Version? = null

        val idxWords: MutableMap<POS, MutableMap<SenseIndexID, SenseIndex>> = makePOSMap<SenseIndexID, SenseIndex>()

        val synsets: MutableMap<POS, MutableMap<SynsetID, Synset>> = makePOSMap<SynsetID, Synset>()

        val exceptions: MutableMap<POS, MutableMap<ExceptionEntryID, ExceptionEntry>> = makePOSMap<ExceptionEntryID, ExceptionEntry>()

        var words: MutableMap<SenseKey, Sense> = makeMap<SenseKey, Sense>(212500, null)

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
         * @param contents the items to be inserted in the map, may be null. If non-null, the initialSize parameter is ignored
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
            compactPOSMap<SenseIndexID, SenseIndex>(idxWords)
            compactPOSMap<SynsetID, Synset>(synsets)
            compactPOSMap<ExceptionEntryID, ExceptionEntry>(exceptions)
            words = compactMap<SenseKey, Sense>(words)
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
         * @param old the word to be replicated
         * @return the new synset, a copy of the first
         */
        private fun makeWord(newSynset: Synset, old: Sense): Sense {

            // related words
            val newRelated = old.related
                .map { (ptr, oldTargets) ->
                    val newTargets: List<ISenseID> = oldTargets
                        .map { it as SenseIDWithNum }
                        .map {
                            val resolver: Map<SynsetID, Synset> = synsets[it.pOS]!!
                            val otherSynset: Synset = resolver[it.synsetID]!!
                            otherSynset.words[it.senseNumber - 1].iD
                        }
                        .toList()
                    ptr to newTargets
                }
                .toMap()

            // word
            val word = Sense(newSynset, old.iD, old.lexicalID, old.adjectiveMarker, old.verbFrames, newRelated)
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
        private fun makeIndexWord(old: SenseIndex): SenseIndex {
            val newIDs: Array<ISenseID> = Array(old.wordIDs.size) { i ->
                var oldID: ISenseID = old.wordIDs[i]
                val resolver = synsets[oldID.pOS]!!
                var synset: Synset = resolver[oldID.synsetID]!!
                val newWord = synset.words.first { it.iD == oldID }
                newWord.iD
            }
            return SenseIndex(old.iD, old.tagSenseCount, newIDs)
        }

        /**
         * A utility class that allows us to build word objects
         *
         * Constructs a new word builder object out of the specified old
         * synset and word.
         *
         * @param oldWord the old word that backs this builder
         */
        inner class WordBuilder(private val oldWord: Sense) : Synset.IWordBuilder {

            override fun toWord(synset: Synset): Sense {
                return makeWord(synset, oldWord)
            }
        }
    }

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