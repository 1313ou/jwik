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

import edu.mit.jwi.ICachingDictionary.IItemCache
import edu.mit.jwi.data.ContentTypeKey
import edu.mit.jwi.data.IHasLifecycle.ObjectClosedException
import edu.mit.jwi.data.compare.ILineComparator
import edu.mit.jwi.item.*
import java.io.IOException
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.Throws

/**
 * A dictionary that caches the results of another dictionary
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.2.0
 */
open class CachingDictionary(
    /**
     * The dictionary that is wrapped by this dictionary; will never
     */
    val backingDictionary: IDictionary,

    ) : ICachingDictionary {

    override val cache: IItemCache = ItemCache()

    /**
     * An internal method for assuring compliance with the dictionary interface
     * that says that methods will throw `ObjectClosedException`s if
     * the dictionary has not yet been opened.
     *
     * @throws ObjectClosedException if the dictionary is closed.
     * @since JWI 2.2.0
     */
    protected fun checkOpen() {
        if (isOpen) {
            if (!cache.isOpen) {
                try {
                    cache.open()
                } catch (e: IOException) {
                    throw ObjectClosedException(e)
                }
            }
        } else {
            if (cache.isOpen) {
                cache.close()
            }
            throw ObjectClosedException()
        }
    }

    override var charset
        get() = backingDictionary.charset
        set(charset) {
            backingDictionary.charset = charset
        }

    override fun setComparator(contentType: ContentTypeKey, comparator: ILineComparator?) {
        backingDictionary.setComparator(contentType, comparator)
    }

    override fun setSourceMatcher(contentTypeKey: ContentTypeKey, pattern: String?) {
        backingDictionary.setSourceMatcher(contentTypeKey, pattern)
    }

    @Throws(IOException::class)
    override fun open(): Boolean {
        if (isOpen) {
            return true
        }
        cache.open()
        return backingDictionary.open()
    }

    override val isOpen: Boolean
        get() = backingDictionary.isOpen

    override fun close() {
        if (!isOpen) {
            return
        }
        cache.close()
        backingDictionary.close()
    }

    override val version: Version?
        get() {
            return backingDictionary.version
        }

    override fun getIndexWord(lemma: String, pos: POS): IndexWord? {
        checkOpen()
        val id: IndexWordID = IndexWordID(lemma, pos)
        var item = cache.retrieveItem<IndexWord, IndexWordID>(id)
        if (item == null) {
            item = backingDictionary.getIndexWord(id)
            if (item != null) {
                cache.cacheItem(item)
            }
        }
        return item
    }

    override fun getIndexWord(id: IndexWordID): IndexWord? {
        checkOpen()
        var item = cache.retrieveItem<IndexWord, IndexWordID>(id)
        if (item == null) {
            item = backingDictionary.getIndexWord(id)
            if (item != null) {
                cache.cacheItem(item)
            }
        }
        return item
    }

    override fun getIndexWordIterator(pos: POS): Iterator<IndexWord> {
        return backingDictionary.getIndexWordIterator(pos)
    }

    override fun getWord(id: IWordID): Word? {
        checkOpen()
        var item = cache.retrieveItem<Word, IWordID>(id)
        if (item == null) {
            item = backingDictionary.getWord(id)
            if (item != null) {
                val s = checkNotNull(item.synset)
                cacheSynset(s)
            }
        }
        return item
    }

    override fun getWord(key: SenseKey): Word? {
        checkOpen()
        var item = cache.retrieveWord(key)
        if (item == null) {
            item = backingDictionary.getWord(key)
            if (item != null) {
                val s = checkNotNull(item.synset as Synset)
                cacheSynset(s)
            }
        }
        return item
    }

    override fun getSynset(id: SynsetID): Synset? {
        checkOpen()
        var item = cache.retrieveItem<Synset, SynsetID>(id)
        if (item == null) {
            item = backingDictionary.getSynset(id)
            if (item != null) {
                cacheSynset(item)
            }
        }
        return item
    }

    /**
     * Caches the specified synset and its words.
     *
     * @param synset the synset to be cached; may not be null
     * @throws NullPointerException if the specified synset is null
     * @since JWI 2.2.0
     */
    protected fun cacheSynset(synset: Synset) {
        val cache: IItemCache = cache
        cache.cacheItem(synset)
        for (word in synset.words) {
            cache.cacheItem(word)
            cache.cacheWordByKey(word)
        }
    }

    override fun getSynsetIterator(pos: POS): Iterator<Synset> {
        return backingDictionary.getSynsetIterator(pos)
    }

    override fun getSenseEntry(key: SenseKey): SenseEntry? {
        checkOpen()
        var entry = cache.retrieveSenseEntry(key)
        if (entry == null) {
            entry = backingDictionary.getSenseEntry(key)
            if (entry != null) {
                cache.cacheSenseEntry(entry)
            }
        }
        return entry
    }

    override fun getSenseEntryIterator(): Iterator<SenseEntry> {
        checkNotNull(this.backingDictionary)
        return backingDictionary.getSenseEntryIterator()
    }

    override fun getExceptionEntry(surfaceForm: String, pos: POS): ExceptionEntry? {
        checkOpen()
        val id = ExceptionEntryID(surfaceForm, pos)
        var item = cache.retrieveItem<ExceptionEntry, ExceptionEntryID>(id)
        if (item == null) {
            item = backingDictionary.getExceptionEntry(id)
            if (item != null) {
                cache.cacheItem(item)
            }
        }
        return item
    }

    override fun getExceptionEntry(id: ExceptionEntryID): ExceptionEntry? {
        checkOpen()
        var item = cache.retrieveItem<ExceptionEntry, ExceptionEntryID>(id)
        if (item == null) {
            item = backingDictionary.getExceptionEntry(id)
            if (item != null) {
                cache.cacheItem(item)
            }
        }
        return item
    }

    override fun getExceptionEntryIterator(pos: POS): Iterator<ExceptionEntry> {
        return backingDictionary.getExceptionEntryIterator(pos)
    }

    /**
     * An LRU cache for objects in JWI.
     *
     * Caller can specify both the initial size, maximum size, and the
     * initial state of caching.
     *
     * Default constructor that initializes the dictionary with caching enabled.
     *
     * @param initialCapacity0  the initial capacity of the cache
     * @param maximumCapacity0  the maximum capacity of the cache
     * @param isEnabled0        whether the cache starts out enabled
     *
     * @author Mark A. Finlayson
     * @version 2.4.0
     * @since JWI 2.2.0
     */
    class ItemCache @JvmOverloads constructor(
        initialCapacity0: Int = DEFAULT_INITIAL_CAPACITY,
        maximumCapacity0: Int = DEFAULT_MAXIMUM_CAPACITY,
        isEnabled0: Boolean = true,
    ) : IItemCache {

        private val lifecycleLock: Lock = ReentrantLock()

        // The caches themselves
        var itemCache: MutableMap<IItemID, IItem<*>>? = null

        var keyCache: MutableMap<SenseKey, Word>? = null

        var senseCache: MutableMap<SenseKey, SenseEntry>? = null

        var sensesCache: MutableMap<SenseKey, Array<SenseEntry>>? = null

        var initialCapacity: Int = initialCapacity0
            set(capacity) {
                field = if (capacity < 1) DEFAULT_INITIAL_CAPACITY else capacity

            }

        override var maximumCapacity: Int = maximumCapacity0
            set(capacity) {
                val oldCapacity = maximumCapacity
                maximumCapacity = capacity
                if (maximumCapacity < 1 || oldCapacity <= maximumCapacity) {
                    return
                }
                reduceCacheSize(itemCache!!)
                reduceCacheSize(keyCache!!)
                reduceCacheSize(senseCache!!)
                reduceCacheSize(sensesCache!!)
            }

        override var isEnabled: Boolean = isEnabled0

        override fun open(): Boolean {
            if (isOpen) {
                return true
            }
            try {
                lifecycleLock.lock()
                val capacity = if (initialCapacity < 1) DEFAULT_INITIAL_CAPACITY else initialCapacity
                itemCache = makeCache<IItemID, IItem<*>>(capacity)
                keyCache = makeCache<SenseKey, Word>(capacity)
                senseCache = makeCache<SenseKey, SenseEntry>(capacity)
                sensesCache = makeCache<SenseKey, Array<SenseEntry>>(capacity)
            } finally {
                lifecycleLock.unlock()
            }
            return true
        }

        /**
         * Creates the map that backs this cache.
         *
         * @param <K>             the key type
         * @param <V>             the value type
         * @param initialCapacity the initial capacity
         * @return the new map
         * @since JWI 2.2.0
         */
        private fun <K, V> makeCache(initialCapacity: Int): MutableMap<K, V> {
            return LinkedHashMap<K, V>(initialCapacity, DEFAULT_LOAD_FACTOR, true)
        }

        override val isOpen: Boolean
            get() = itemCache != null && keyCache != null && senseCache != null && sensesCache != null

        /**
         * An internal method for assuring compliance with the dictionary
         * interface that says that methods will throw
         * `ObjectClosedException`s if the dictionary has not yet been
         * opened.
         *
         * @throws ObjectClosedException if the dictionary is closed.
         * @since JWI 2.2.0
         */
        private fun checkOpen() {
            if (!isOpen) {
                throw ObjectClosedException()
            }
        }

        override fun close() {
            if (!isOpen) {
                return
            }
            try {
                lifecycleLock.lock()
                itemCache = null
                keyCache = null
                senseCache = null
                sensesCache = null
            } finally {
                lifecycleLock.unlock()
            }
        }

        override fun clear() {
            itemCache?.clear()
            keyCache?.clear()
            senseCache?.clear()
            sensesCache?.clear()
        }

        override fun size(): Int {
            checkOpen()
            return itemCache!!.size + keyCache!!.size + senseCache!!.size + sensesCache!!.size
        }

        override fun cacheItem(item: IItem<*>) {
            checkOpen()
            if (!isEnabled) {
                return
            }
            val id = checkNotNull(item.iD)
            itemCache!!.put(id, item)
            reduceCacheSize(itemCache!!)
        }

        override fun cacheWordByKey(word: Word) {
            checkOpen()
            if (!isEnabled) {
                return
            }
            checkNotNull(keyCache)
            keyCache!!.put(word.senseKey, word)
            reduceCacheSize(keyCache!!)
        }

        override fun cacheSenseEntry(entry: SenseEntry) {
            checkOpen()
            if (!isEnabled) {
                return
            }
            val sk = checkNotNull(entry.senseKey)
            checkNotNull(senseCache)
            senseCache!!.put(sk, entry)
            reduceCacheSize(senseCache!!)
        }

        private val cacheLock = Any()

        /**
         * Brings the map size into line with the specified maximum capacity of
         * this cache.
         *
         * @param cache the map to be trimmed
         * @since JWI 2.2.0
         */
        private fun reduceCacheSize(cache: MutableMap<*, *>) {
            if (!isOpen || maximumCapacity < 1 || cache.size < maximumCapacity) {
                return
            }
            synchronized(cacheLock) {
                val remove = cache.size - maximumCapacity
                val itr: MutableIterator<*> = cache.keys.iterator()
                repeat(remove + 1) {
                    if (itr.hasNext()) {
                        itr.next()
                        itr.remove()
                    }
                }
            }
        }

        override fun <T : IItem<D>, D : IItemID> retrieveItem(id: D): T? {
            checkOpen()
            @Suppress("UNCHECKED_CAST")
            return itemCache!![id] as T?
        }

        override fun retrieveWord(key: SenseKey): Word? {
            checkOpen()
            return keyCache!![key]
        }

        override fun retrieveSenseEntry(key: SenseKey): SenseEntry? {
            checkOpen()
            return senseCache!![key]
        }

        companion object {

            const val DEFAULT_INITIAL_CAPACITY: Int = 16
            const val DEFAULT_MAXIMUM_CAPACITY: Int = 512
            const val DEFAULT_LOAD_FACTOR: Float = 0.75f
        }
    }

    override fun getWords(start: String, pos: POS?, limit: Int): Set<String> {
        checkOpen()
        return backingDictionary.getWords(start, pos, limit)
    }
}
