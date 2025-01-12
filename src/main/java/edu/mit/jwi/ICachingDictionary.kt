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

import edu.mit.jwi.data.IHasLifecycle
import edu.mit.jwi.item.*

/**
 * Provides a governing interface for dictionaries that cache their results.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.2.0
 */
interface ICachingDictionary : IDictionary {

    val cache: IItemCache?

    /**
     * The cache used by a caching dictionary.
     *
     * @author Mark A. Finlayson
     * @version 2.4.0
     * @since JWI 2.2.0
     */
    interface IItemCache : IHasLifecycle {

        /**
         * true if this cache is enabled;
         * false otherwise.
         * If a cache is enabled, it will cache an item passed to its `cache` methods.
         *
         * @since JWI 2.2.0
         */
        var isEnabled: Boolean

        /**
         * The maximum capacity of this cache.
         *
         * @since JWI 2.2.0
         */
        var maximumCapacity: Int

        /**
         * Returns the number of items in the cache.
         *
         * @return the number of items in the cache.
         * @since JWI 2.2.0
         */
        fun size(): Int

        /**
         * Caches the specified item, if this cache is enabled. Otherwise, does nothing.
         *
         * @param item the item to be cached; may not be null
         * @since JWI 2.2.0
         */
        fun cacheItem(item: IItem<*>)

        /**
         * Caches the specified word, indexed by its sense key.
         *
         * @param word the word to be cached; may not be null
         * @since JWI 2.2.0
         */
        fun cacheWordByKey(word: Word)

        /**
         * Caches the specified entry.
         *
         * @param entry the entry to be cached; may not be null
         * @since JWI 2.2.0
         */
        fun cacheSenseEntry(entry: SenseEntry)

        /**
         * Retrieves the item identified by the specified id.
         *
         * @param <T> the type of the item
         * @param <D> the type of the item id
         * @param id  the id for the requested item
         * @return the item for the specified id, or null if not present in the cache
         * @since JWI 2.2.0
         */
        fun <T, D> retrieveItem(id: D): T? where
                T : IItem<D>,
                D : IItemID

        /**
         * Retrieves the word identified by the specified sense key.
         *
         * @param key the sense key for the requested word
         * @return the word for the specified key, or null if not
         * present in the cache
         * @since JWI 2.2.0
         */
        fun retrieveWord(key: SenseKey): Word?

        /**
         * Retrieves the sense entry identified by the specified sense key.
         *
         * @param key the sense key for the requested sense entry
         * @return the sense entry for the specified key, or null if not present in the cache
         * @since JWI 2.2.0
         */
        fun retrieveSenseEntry(key: SenseKey): SenseEntry?

        /**
         * Removes all entries from the cache.
         *
         * @since JWI 2.2.0
         */
        fun clear()
    }
}
