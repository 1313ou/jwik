/* ******************************************************************************
 * Java Wordnet Interface Library (JWI) v2.4.0
 * Copyright (c) 2007-2015 Mark A. Finlayson
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 4.0
 * International Public License, which means it may be freely used for all
 * purposes, as long as proper acknowledgment is made.  See the license file
 * included with this distribution for more details.
 *******************************************************************************/
package edu.mit.jwi.data

import edu.mit.jwi.item.IHasVersion

/**
 * An object that mediate between an DataSourceDictionary and the data that is contained in the dictionary data resources.
 * Data resources are assigned a name (e.g., *verb.data*, for the data resource pertaining to verbs) and a content type.
 * Data resources are assumed to be indexed by keys that can be passed into the [.getLine] method to find a particular piece of data in the resource.
 * The `String` return can be parsed by the parser associated with the content type to produce a data object (e.g., an `Synset` or `IndexWord` object).
 *
 * @param <T> the type of object represented in this data resource
 */
interface IDataSource<T> : IHasVersion, Iterable<String>, IHasLifecycle {

    val name: String

    val contentType: ContentType<T>

    /**
     * Returns the line in the resource contains the data indexed by the specified key.
     * If the file cannot find the key in its data resource, it returns null
     *
     * @param key the key which indexes the desired data
     * @return the line indexed by the specified key in the resource
     */

    fun getLine(key: String): String?

    /**
     * Returns an iterator that will iterator over lines in the data resource, starting at the line specified by the given key.
     * If the key is null, this is the same as calling the plain * [.iterator] method.
     * If no line starts with the pattern, the iterator's hasNext will return false.
     *
     * @param key the key at which the iterator should begin
     * @return an iterator that will iterate over the file starting at the line indexed by the specified key
     */
    fun iterator(key: String?): Iterator<String>
}
