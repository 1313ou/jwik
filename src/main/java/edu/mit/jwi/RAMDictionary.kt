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

import edu.mit.jwi.IRAMDictionary.IInputStreamFactory
import edu.mit.jwi.data.ContentTypeKey
import edu.mit.jwi.data.FileProvider
import edu.mit.jwi.data.IHasLifecycle.LifecycleState
import edu.mit.jwi.data.IHasLifecycle.ObjectOpenException
import edu.mit.jwi.data.ILoadPolicy
import edu.mit.jwi.data.compare.ILineComparator
import edu.mit.jwi.item.*
import edu.mit.jwi.item.Synset.IWordBuilder
import java.io.*
import java.net.URL
import java.nio.charset.Charset
import java.util.concurrent.Callable
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.Throws

/**
 * Default implementation of the `IRAMDictionary` interface. This
 * implementation is designed to wrap an arbitrary dictionary object; however,
 * convenience constructors are provided for the most common use cases:
 *
 *  * Wordnet files located on the local file system
 *  * Wordnet data to be loaded into memory from an exported stream
 *
 *
 *
 * **Note:** If you receive an [OutOfMemoryError] while using this
 * object (this can occur on 32 bit JVMs), try increasing your heap size, for
 * example, by using the `-Xmx` switch.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.2.0
 */
class RAMDictionary private constructor(
    backing: IDictionary?,
    factory: IInputStreamFactory?,
    loadPolicy: Int,
) : IRAMDictionary {

    /**
     * Returns the dictionary that backs this instance.
     *
     * @return the dictionary that backs this instance; may be null.
     * @since JWI 2.2.0
     */
    val backingDictionary: IDictionary?

    /**
     * Returns the stream factory that backs this instance; may be null.
     *
     * @return the stream factory that backs this instance; may be null
     * @since JWI 2.4.0
     */
    val streamFactory: IInputStreamFactory?

    private val lifecycleLock: Lock = ReentrantLock()

    private val loadLock: Lock = ReentrantLock()

    @Volatile
    private var state: LifecycleState = LifecycleState.CLOSED

    @Transient
    private var loader: Thread? = null

    private var data: DictionaryData? = null

    /**
     * Loads data from the specified File using the specified load policy. Note
     * that if the file points to a resource that is the exported image of an
     * in-memory dictionary, the specified load policy is ignored: the
     * dictionary is loaded into memory immediately.
     *
     * @throws NullPointerException if the specified file is null
     * @see ILoadPolicy

     * Constructs a new wrapper RAM dictionary that will load the contents the
     * specified local Wordnet data, with the specified load policy. Note that
     * if the file points to an exported image of an in-memory dictionary, the
     * required load policy is to load immediately.
     *
     * @param file       a file pointing to a local copy of wordnet; may not be null
     * @param loadPolicy the load policy of the dictionary; see constants in
     * [ILoadPolicy]. Note that if the file points to a
     * resource that is the exported image of an in-memory
     * dictionary, the specified load policy is ignored: the
     * dictionary is loaded into memory immediately.
     * @throws NullPointerException if the specified file is null
     * @since JWI 2.4.0
     */
    @JvmOverloads
    constructor(
        file: File,
        loadPolicy: Int = DEFAULT_LOAD_POLICY,
    ) : this(createBackingDictionary(file)!!, createInputStreamFactory(file)!!, loadPolicy)

    /**
     * Loads data from the specified URL using the specified load policy. Note
     * that if the url points to a resource that is the exported image of an
     * in-memory dictionary, the specified load policy is ignored: the
     * dictionary is loaded into memory immediately.
     *
     * @throws NullPointerException if the specified url is null
     * @see ILoadPolicy
     *
     * Constructs a new RAMDictionary that will load the contents the specified
     * Wordnet data using the default load policy. Note that if the url points
     * to a resource that is the exported image of an in-memory dictionary, the
     * required load policy is to load immediately.
     *
     * @param url        an url pointing to a local copy of wordnet; may not be null
     * @param loadPolicy the load policy of the dictionary; see constants in
     * [ILoadPolicy]. Note that if the url points to a
     * resource that is the exported image of an in-memory
     * dictionary, the specified load policy is ignored: the
     * dictionary is loaded into memory immediately.
     * @throws NullPointerException if the specified url is null
     * @since JWI 2.4.0
     */
    @JvmOverloads
    constructor(
        url: URL,
        loadPolicy: Int = DEFAULT_LOAD_POLICY,
    ) : this(createBackingDictionary(url)!!, createInputStreamFactory(url)!!, loadPolicy)

    /**
     * Constructs a new RAMDictionary that will load the contents of
     * the wrapped dictionary into memory, with the specified load policy.
     *
     * @param dict       the dictionary to be wrapped, may not be null
     * @param loadPolicy the load policy of the dictionary; see constants in
     * [ILoadPolicy].
     * @see ILoadPolicy
     *
     * @since JWI 2.2.0
     */
    constructor(dict: IDictionary, loadPolicy: Int) : this(dict, null, loadPolicy)

    /**
     * Constructs a new RAMDictionary that will load an in-memory image from the
     * specified stream factory.
     *
     * @param factory the stream factory that provides the stream; may not be null
     * @throws NullPointerException if the factory is null
     * @since JWI 2.4.0
     */
    constructor(factory: IInputStreamFactory) : this(null, factory, ILoadPolicy.IMMEDIATE_LOAD)

    /**
     * This is an internal constructor that unifies the constructor decision
     * matrix. Exactly one of the backing dictionary or the input factory must
     * be non-null, otherwise an exception is thrown. If the
     * factory is non-null, the dictionary will ignore the
     * specified load policy and set the load policy to "immediate load".
     *
     * @param backing    the backing dictionary; may be null
     * @param factory    the input stream factory; may be null
     * @param loadPolicy the load policy
     * @since JWI 2.4.0
     */
    init {
        if (backing == null && factory == null) {
            throw NullPointerException()
        }
        check(!(backing != null && factory != null)) { "Both backing dictionary and input stream factory may not be non-null" }

        backingDictionary = backing
        streamFactory = factory
    }

    override var loadPolicy: Int = if (factory == null) loadPolicy else ILoadPolicy.IMMEDIATE_LOAD
        set(policy) {
            if (isOpen)
                throw ObjectOpenException()

            // if the dictionary uses an input stream factory
            // the load policy is effectively IMMEDIATE_LOAD
            // so the load policy is set to this for information purposes
            loadPolicy = if (streamFactory == null) policy else ILoadPolicy.IMMEDIATE_LOAD
        }

    override var charset: Charset?
        get() {
            return backingDictionary?.charset
        }
        set(charset) {
            if (isOpen)
                throw ObjectOpenException()
            backingDictionary?.charset = charset
        }

    override fun setComparator(contentTypeKey: ContentTypeKey, comparator: ILineComparator?) {
        if (isOpen)
            throw ObjectOpenException()
        backingDictionary?.setComparator(contentTypeKey, comparator)
    }

    override fun setSourceMatcher(contentTypeKey: ContentTypeKey, pattern: String?) {
        if (isOpen)
            throw ObjectOpenException()
        backingDictionary?.setSourceMatcher(contentTypeKey, pattern)
    }

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
            if (state == LifecycleState.CLOSED || state == LifecycleState.CLOSING) {
                return
            }

            if (loader != null) {
                return
            }
            loader = Thread(JWIBackgroundDataLoader())
            loader!!.setName(JWIBackgroundDataLoader::class.java.getSimpleName())
            loader!!.setDaemon(true)
            loader!!.start()
            if (block) {
                loader!!.join()
            }
        } finally {
            loadLock.unlock()
        }
    }

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

            if (backingDictionary == null) {
                // behavior when loading from an
                // input stream is immediate load
                try {
                    load(true)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    return false
                }
                return true
            } else {
                // behavior when loading from a
                // backing dictionary depends on the
                // load policy
                val result = backingDictionary.open()
                if (result) {
                    try {
                        when (loadPolicy) {
                            ILoadPolicy.IMMEDIATE_LOAD  -> load(true)
                            ILoadPolicy.BACKGROUND_LOAD -> load(false)
                        }
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                        return false
                    }
                }
                return result
            }
        } finally {
            // make sure to clear the opening state
            state = assertLifecycleState()
            lifecycleLock.unlock()
        }
    }

    override val isOpen: Boolean
        get() {
            try {
                lifecycleLock.lock()
                return state == LifecycleState.OPEN
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
            backingDictionary?.close()

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
     * @since JWI 2.4.0
     */
    private fun assertLifecycleState(): LifecycleState {
        try {
            lifecycleLock.lock()

            // if the data object is present, then we are open
            if (data != null) {
                return LifecycleState.OPEN
            }

            // if the backing dictionary is present and open, then we are open
            if (backingDictionary != null && backingDictionary.isOpen) {
                return LifecycleState.OPEN
            }

            // otherwise we are closed
            return LifecycleState.CLOSED
        } finally {
            lifecycleLock.unlock()
        }
    }

    @Throws(IOException::class)
    override fun export(out: OutputStream) {
        var out = out
        try {
            loadLock.lock()
            check(isLoaded) { "RAMDictionary not loaded into memory" }

            out = GZIPOutputStream(out)
            out = BufferedOutputStream(out)
            val oos = ObjectOutputStream(out)

            oos.writeObject(data)
            oos.flush()
            oos.close()
        } finally {
            loadLock.unlock()
        }
    }

    override val version: Version?
        get() {
            if (backingDictionary != null) {
                return backingDictionary.version
            }
            if (data != null) {
                return data!!.version
            }
            return null
        }

    // F I N D

    // INDEXWORD

    override fun getIndexWord(lemma: String, pos: POS): IndexWord? {
        return getIndexWord(IndexWordID(lemma, pos))
    }

    override fun getIndexWord(id: IndexWordID): IndexWord? {
        if (data != null) {
            val m = data!!.idxWords[id.pOS]!!
            return m[id]
        } else {
            checkNotNull(backingDictionary)
            return backingDictionary.getIndexWord(id)
        }
    }

    override fun getIndexWordIterator(pos: POS): Iterator<IndexWord> {
        return HotSwappableIndexWordIterator(pos)
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
        } else {
            checkNotNull(backingDictionary)
            return backingDictionary.getWord(id)
        }
    }

    override fun getWord(key: SenseKey): Word? {
        if (data != null) {
            return data!!.words[key]
        } else {
            checkNotNull(backingDictionary)
            return backingDictionary.getWord(key)
        }
    }

    // SYNSET

    override fun getSynset(id: SynsetID): Synset? {
        if (data != null) {
            val m = checkNotNull(data!!.synsets[id.pOS])
            return m[id]
        } else {
            checkNotNull(backingDictionary)
            return backingDictionary.getSynset(id)
        }
    }

    override fun getSynsetIterator(pos: POS): Iterator<Synset> {
        return HotSwappableSynsetIterator(pos)
    }

    // SENSE ENTRY

    override fun getSenseEntry(key: SenseKey): SenseEntry? {
        if (data != null) {
            return data!!.senses[key]
        } else {
            checkNotNull(backingDictionary)
            return backingDictionary.getSenseEntry(key)
        }
    }

    override fun getSenseEntryIterator(): Iterator<SenseEntry> {
        return HotSwappableSenseEntryIterator()
    }

    // EXCEPTION ENTRY

    override fun getExceptionEntry(surfaceForm: String, pos: POS): IExceptionEntry? {
        return getExceptionEntry(ExceptionEntryID(surfaceForm, pos))
    }

    override fun getExceptionEntry(id: IExceptionEntryID): IExceptionEntry? {
        if (data != null) {
            val m = checkNotNull(data!!.exceptions[id.pOS])
            return m[id]
        } else {
            checkNotNull(backingDictionary)
            return backingDictionary.getExceptionEntry(id)
        }
    }

    override fun getExceptionEntryIterator(pos: POS): Iterator<IExceptionEntry> {
        return HotSwappableExceptionEntryIterator(pos)
    }

    /**
     * An iterator that allows the dictionary to be loaded into memory while it
     * is iterating.
     *
     * Constructs a new hot swappable iterator.
     *
     * @param itr          the wrapped iterator
     * @param checkForLoad if true, on each call the iterator checks to
     * see if the dictionary has been loaded into memory,
     * switching data sources if so
     * @param <E> the element type of the iterator
     * @author Mark A. Finlayson
     * @since JWI 2.2.0
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
            checkNotNull(itr)
            return itr.hasNext()
        }

        override fun next(): E {
            if (checkForLoad) {
                checkForLoad()
                checkNotNull(itr)
                last = itr.next()
                return last!!
            } else {
                checkNotNull(itr)
                return itr.next()
            }
        }

        /**
         * Checks to see if the data has been loaded into memory; is so,
         * replaces the original iterator with one that iterates over the
         * in-memory data structures.
         *
         * @since JWI 2.2.0
         */
        fun checkForLoad() {
            if (data == null) {
                return
            }
            checkForLoad = false
            itr = makeIterator()
            if (last != null) {
                var consume: E?
                while (itr.hasNext()) {
                    consume = itr.next()
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
         * @since JWI 2.2.0
         */
        abstract fun makeIterator(): Iterator<E>
    }

    /**
     * A hot swappable iterator for index words.
     *
     * @param pos the part of speech for the iterator
     *
     * @author Mark A. Finlayson
     * @since JWI 2.2.0
     */
    private inner class HotSwappableIndexWordIterator(private val pos: POS) :
        HotSwappableIterator<IndexWord>(
            if (data == null) backingDictionary!!.getIndexWordIterator(pos) else data!!.idxWords[pos]!!.values.iterator(),
            data == null
        ) {

        override fun makeIterator(): Iterator<IndexWord> {
            checkNotNull(data)
            val m = checkNotNull(data!!.idxWords[pos])
            return m.values.iterator()
        }
    }

    /**
     * A hot swappable iterator for synsets.
     *
     * @param pos the part of speech for the iterator
     *
     * @author Mark A. Finlayson
     * @since JWI 2.2.0
     */
    private inner class HotSwappableSynsetIterator(private val pos: POS) :
        HotSwappableIterator<Synset>(
            if (data == null) backingDictionary!!.getSynsetIterator(pos) else data!!.synsets[pos]!!.values.iterator(),
            data == null
        ) {

        override fun makeIterator(): Iterator<Synset> {
            checkNotNull(data)
            val m = checkNotNull(data!!.synsets[pos])
            return m.values.iterator()
        }
    }

    /**
     * A hot swappable iterator that iterates over exceptions entries for a
     * particular part of speech.
     *
     * @param pos the part of speech for this iterator, may not be null
     *
     * @author Mark A. Finlayson
     * @since JWI 2.2.0
     */
    private inner class HotSwappableExceptionEntryIterator(private val pos: POS) :
        HotSwappableIterator<IExceptionEntry>(
            if (data == null) backingDictionary!!.getExceptionEntryIterator(pos) else data!!.exceptions[pos]!!.values.iterator(),
            data == null
        ) {

        override fun makeIterator(): Iterator<IExceptionEntry> {
            checkNotNull(data)
            val m = checkNotNull(data!!.exceptions[pos])
            return m.values.iterator()
        }
    }

    /**
     * A hot swappable iterator that iterates over sense entries.
     *
     * @author Mark A. Finlayson
     * @since JWI 2.2.0
     */
    private inner class HotSwappableSenseEntryIterator :
        HotSwappableIterator<SenseEntry>(
            if (data == null) backingDictionary!!.getSenseEntryIterator() else data!!.senses.values.iterator(),
            data == null
        ) {

        override fun makeIterator(): Iterator<SenseEntry> {
            checkNotNull(data)
            return data!!.senses.values.iterator()
        }
    }

    /**
     * This runnable loads the dictionary data into memory and sets the
     * appropriate variable in the parent dictionary.
     *
     * @author Mark A. Finlayson
     * @since JWI 2.2.0
     */
    private inner class JWIBackgroundDataLoader : Runnable {

        override fun run() {
            try {
                if (backingDictionary == null) {
                    // if there is no backing dictionary from
                    // which to load our data, load it from the
                    // stream factory
                    checkNotNull(streamFactory)
                    var `in` = streamFactory.makeInputStream()
                    `in` = GZIPInputStream(`in`)
                    `in` = BufferedInputStream(`in`)

                    // read the dictionary data
                    val ois = ObjectInputStream(`in`)
                    data = ois.readObject() as DictionaryData?
                    `in`.close()
                } else {
                    // here we have a backing dictionary from
                    // which we should load our data
                    val loader = DataLoader(backingDictionary)
                    this@RAMDictionary.data = loader.call()
                    backingDictionary.close()
                }
            } catch (t: Throwable) {
                if (!Thread.currentThread().isInterrupted) {
                    t.printStackTrace()
                    System.err.println("Unable to load dictionary data into memory")
                }
            }
        }
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
     * @throws NullPointerException if the specified dictionary is null
     * @author Mark A. Finlayson
     * @since JWI 2.2.0
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
                        checkNotNull(idxWords)
                        val id = checkNotNull(idxWord.iD)
                        idxWords.put(id, idxWord)
                    }
                }
                if (t.isInterrupted) {
                    return null
                }

                // synsets and words
                var synsets = result.synsets[pos]
                checkNotNull(synsets)
                run {
                    val i: Iterator<Synset> = source.getSynsetIterator(pos)
                    while (i.hasNext()) {
                        val synset = i.next()
                        val id = checkNotNull(synset.iD)
                        synsets.put(id, synset)
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
                checkNotNull(exceptions)
                val i: Iterator<IExceptionEntry> = source.getExceptionEntryIterator(pos)
                while (i.hasNext()) {
                    val exception = i.next()
                    checkNotNull(exception.iD)
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
                if (word == null) {
                    throw NullPointerException()
                }
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
         * @throws NullPointerException if either argument is null
         * @since JWI 2.2.0
         */
        private fun makeSenseEntry(key: SenseKey, old: SenseEntry): SenseEntry {
            return SenseEntry(key, old.offset, old.senseNumber, old.tagCount)
        }
    }

    /**
     * Object that holds all the dictionary data loaded from the Wordnet files.
     *
     * @author Mark A. Finlayson
     * @since JWI 2.2.0
     */
    class DictionaryData : Serializable {

        var version: Version? = null

        val idxWords: MutableMap<POS, MutableMap<IndexWordID, IndexWord>>

        val synsets: MutableMap<POS, MutableMap<SynsetID, Synset>>

        val exceptions: MutableMap<POS, MutableMap<IExceptionEntryID, IExceptionEntry>>

        var words: MutableMap<SenseKey, Word>

        var senses: MutableMap<SenseKey, SenseEntry>

        /**
         * Constructs an empty dictionary data object.
         *
         * @since JWI 2.2.0
         */
        init {
            idxWords = makePOSMap<IndexWordID, IndexWord>()
            synsets = makePOSMap<SynsetID, Synset>()
            exceptions = makePOSMap<IExceptionEntryID, IExceptionEntry>()
            words = makeMap<SenseKey, Word>(208000, null)
            senses = makeMap<SenseKey, SenseEntry>(208000, null)
        }

        /**
         * This method is used when constructing the dictionary data object.
         * Constructs a map with an empty sub-map for every part of speech.
         * Subclasses may override to change map character
         *
         * @param <K> the type of the keys for the sub-maps
         * @param <V> the type of the values for the sub-maps
         * @return a map with an empty sub-map for every part of speech.
         * @since JWI 2.2.0
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
         * @param initialSize the initial size of the map; this parameter is ignored if
         * the `contents` parameter is non-null.
         * @param contents    the items to be inserted in the map, may be null.
         * If non-null, the `initialSize` parameter is ignored
         * @return an empty map with either the specified initial size, or
         * contained the specified contents
         * @throws IllegalArgumentException if the initial size is invalid (less than 1) and the
         * specified contents are null
         * @since JWI 2.2.0
         */
        private fun <K, V> makeMap(initialSize: Int, contents: MutableMap<K, V>?): MutableMap<K, V> {
            return if (contents == null) LinkedHashMap<K, V>(initialSize) else LinkedHashMap<K, V>(contents)
        }

        /**
         * Compacts this dictionary data object by resizing the internal maps,
         * and removing redundant objects where possible.
         *
         * @since JWI 2.2.0
         */
        fun compact() {
            compactSize()
            compactObjects()
        }

        /**
         * Resizes the internal data maps to be the exact size to contain their
         * data.
         *
         * @since JWI 2.2.0
         */
        fun compactSize() {
            compactPOSMap<IndexWordID, IndexWord>(idxWords)
            compactPOSMap<SynsetID, Synset>(synsets)
            compactPOSMap<IExceptionEntryID, IExceptionEntry>(exceptions)
            words = compactMap<SenseKey, Word>(words)
            senses = compactMap<SenseKey, SenseEntry>(senses)
        }

        /**
         * Compacts a part-of-speech map
         *
         * @param map the part-of-speech keyed map to be compacted
         * @param <K> key type
         * @param <V> value type
         * @since JWI 2.2.0
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
         * @since JWI 2.2.0
         */
        private fun <K, V> compactMap(map: MutableMap<K, V>): MutableMap<K, V> {
            return makeMap<K, V>(-1, map)
        }

        /**
         * Replaces redundant objects where possible
         *
         * @since JWI 2.2.0
         */
        fun compactObjects() {
            for (pos in POS.entries) {
                val sMap = checkNotNull(synsets[pos])
                for (entry in sMap.entries) {
                    entry.setValue(makeSynset(entry.value))
                }
                val iMap = checkNotNull(idxWords[pos])
                for (entry in iMap.entries) {
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
         * @since JWI 2.2.0
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
         * Creates a new word object that replaces all the old internal `IWordID` objects with those from the denoted words,
         * thus throwing away redundant word ids.
         *
         * @param newSynset the synset for which the word is being made
         * @param old       the word to be replicated
         * @return the new synset, a copy of the first
         * @since JWI 2.2.0
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
            val word: Word = Word(newSynset, old.iD as WordLemmaID, old.lexicalID, old.adjectiveMarker, old.verbFrames, newRelated)
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
         * @since JWI 2.2.0
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
         *
         * @author Mark A. Finlayson
         * @version 2.4.0
         * @since JWI 2.2.0
         */
        inner class WordBuilder(private val oldWord: Word) : IWordBuilder {

            override fun toWord(synset: Synset): Word {
                return makeWord(synset, oldWord)
            }
        }
    }

    override fun getWords(start: String, pos: POS?, limit: Int): Set<String> {
        checkNotNull(backingDictionary)
        return backingDictionary.getWords(start, pos, limit)
    }

    companion object {

        /**
         * The default load policy of a [RAMDictionary] is to load data in the
         * background when opened.
         *
         * @since JWI 2.4.0
         */
        const val DEFAULT_LOAD_POLICY: Int = ILoadPolicy.BACKGROUND_LOAD

        /**
         * Creates an input stream factory out of the specified File. If the file
         * points to a local directory then the method returns null.
         *
         * @param file the file out of which to make an input stream factory; may not
         * be null
         * @return a new input stream factory, or null if the url
         * points to a local directory.
         * @throws NullPointerException if the specified file is null
         * @since JWI 2.4.0
         */
        fun createInputStreamFactory(file: File): IInputStreamFactory? {
            return if (FileProvider.isLocalDirectory(file)) null else IRAMDictionary.FileInputStreamFactory(file)
        }

        /**
         * Creates an input stream factory out of the specified URL. If the url
         * points to a local directory then the method returns null.
         *
         * @param url the url out of which to make an input stream factory; may not
         * be null
         * @return a new input stream factory, or null if the url
         * points to a local directory.
         * @throws NullPointerException if the specified url is null
         * @since JWI 2.4.0
         */
        fun createInputStreamFactory(url: URL): IInputStreamFactory? {
            return if (FileProvider.isLocalDirectory(url)) null else IRAMDictionary.URLInputStreamFactory(url)
        }

        /**
         * Creates a [DataSourceDictionary] out of the specified file, as long
         * as the file points to an existing local directory.
         *
         * @param file the local directory for which to create a data source
         * dictionary; may not be null
         * @return a dictionary object that uses the specified local directory as
         * its data source; otherwise, null
         * @throws NullPointerException if the specified file is null
         * @since JWI 2.4.0
         */
        fun createBackingDictionary(file: File): IDictionary? {
            return if (FileProvider.isLocalDirectory(file)) DataSourceDictionary(FileProvider(file)) else null
        }

        /**
         * Creates a [DataSourceDictionary] out of the specified url, as long
         * as the url points to an existing local directory.
         *
         * @param url the local directory for which to create a data source
         * dictionary; may not be null
         * @return a dictionary object that uses the specified local directory as
         * its data source; otherwise, null
         * @throws NullPointerException if the specified url is null
         * @since JWI 2.4.0
         */
        fun createBackingDictionary(url: URL): IDictionary? {
            return if (FileProvider.isLocalDirectory(url)) DataSourceDictionary(FileProvider(url)) else null
        }

        /**
         * This is a convenience method that transforms a Wordnet dictionary at the
         * specified file location into an in-memory image written to the specified
         * output stream. The file may point to either a directory or in-memory
         * image.
         *
         * @param in  the file from which the Wordnet data should be loaded; may not
         * be null
         * @param out the output stream to which the Wordnet data should be written;
         * may not be null
         * @return true if the export was successful
         * @throws NullPointerException if either argument is null
         * @throws IOException          if there is an IO problem when opening or exporting the
         * dictionary.
         * @since JWI 2.4.0
         */
        @Throws(IOException::class)
        fun export(`in`: File, out: OutputStream): Boolean {
            return export(RAMDictionary(`in`, ILoadPolicy.IMMEDIATE_LOAD), out)
        }

        /**
         * This is a convenience method that transforms a Wordnet dictionary at the
         * specified url location into an in-memory image written to the specified
         * output stream. The url may point to either a directory or in-memory
         * image.
         *
         * @param in  the url from which the Wordnet data should be loaded; may not
         * be null
         * @param out the output stream to which the Wordnet data should be written;
         * may not be null
         * @return true if the export was successful
         * @throws NullPointerException if either argument is null
         * @throws IOException          if there is an IO problem when opening or exporting the
         * dictionary.
         * @since JWI 2.4.0
         */
        @Throws(IOException::class)
        fun export(`in`: URL, out: OutputStream): Boolean {
            return export(RAMDictionary(`in`, ILoadPolicy.IMMEDIATE_LOAD), out)
        }

        /**
         * This is a convenience method that transforms a Wordnet dictionary drawn
         * from the specified input stream factory into an in-memory image written to
         * the specified output stream.
         *
         * @param in  the file from which the Wordnet data should be loaded; may not
         * be null
         * @param out the output stream to which the Wordnet data should be written;
         * may not be null
         * @return true if the export was successful
         * @throws NullPointerException if either argument is null
         * @throws IOException          if there is an IO problem when opening or exporting the
         * dictionary.
         * @since JWI 2.4.0
         */
        @Throws(IOException::class)
        fun export(`in`: IInputStreamFactory, out: OutputStream): Boolean {
            return export(RAMDictionary(`in`), out)
        }

        /**
         * Exports a specified RAM Dictionary object to the specified output stream.
         * This is convenience method.
         *
         * @param dict the dictionary to be exported; the dictionary will be closed
         * at the end of the method.
         * @param out  the output stream to which the data will be written.
         * @return true if the export was successful
         * @throws IOException if there was a IO problem during export
         * @since JWI 2.4.0
         */
        @Throws(IOException::class)
        private fun export(dict: IRAMDictionary, out: OutputStream): Boolean {
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
