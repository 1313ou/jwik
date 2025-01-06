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

import edu.mit.jwi.data.IHasLifecycle.ObjectClosedException
import edu.mit.jwi.data.compare.ILineComparator
import edu.mit.jwi.item.IHasVersion
import edu.mit.jwi.item.POS
import java.net.URL
import java.nio.charset.Charset

/**
 * Objects that implement this interface manage access to data source objects.
 * Before the provider can be used, a client must call [.setSource]
 * (or call the appropriate constructor) followed by [.open].  Otherwise,
 * the provider will throw an exception.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
interface IDataProvider : IHasVersion, IHasLifecycle, IHasCharset {

    /**
     * This method is used to set the source URL from which the provider
     * accesses the data from which it instantiates data sources. The data at
     * the specified location may be in an implementation-specific format. If
     * the provider is currently open, this method throws an
     * `IllegalStateException`.
     *
     * @param source the location of the data, may not be `null`
     * @throws IllegalStateException if the provider is currently open
     * @throws NullPointerException  if the specified `URL` is `null`.
     * @since JWI 1.0
     */
    var source: URL

    /**
     * Sets the character set associated with this dictionary. The character set
     * may be `null`.
     *
     * @param charset the possibly `null` character set to use when
     * decoding files.
     * @throws IllegalStateException if the provider is currently open
     * @since JWI 2.3.4
     */
    fun setCharset(charset: Charset?)

    /**
     * Sets the comparator associated with this content type in this dictionary.
     * The comparator may be `null` in which case it is reset.
     *
     * @param contentTypeKey the `non-null` content type key for which
     * the comparator is to be set.
     * @param comparator     the possibly `null` comparator to use when
     * decoding files.
     * @throws IllegalStateException if the provider is currently open
     * @since JWI 2.4.1
     */
    fun setComparator(contentTypeKey: ContentTypeKey, comparator: ILineComparator?)

    /**
     * Sets pattern attached to content type key, that source files have to
     * match to be selected.
     * This gives selection a first opportunity before falling back on standard data
     * type selection.
     *
     * @param contentTypeKey the `non-null` content type key for which
     * the matcher is to be set.
     * @param pattern        regexp pattern
     * @since JWI 2.4.1
     */
    fun setSourceMatcher(contentTypeKey: ContentTypeKey, pattern: String)

    val types: Set<IContentType<*>>?

    /**
     * Returns the first content type, if any, that matches the specified data
     * type and pos object. Either parameter may be `null`.
     *
     * @param <T> type
     * @param dt  the data type, possibly `null`, of the desired
     * content type
     * @param pos the part of speech, possibly `null`, of the desired
     * content type
     * @return the first content type that matches the specified data type and
     * part of speech.
     * @since JWI 2.3.4
    */
    fun <T> resolveContentType(dt: IDataType<T>, pos: POS?): IContentType<T>?

    /**
     * Returns a data source object for the specified content type, if one is
     * available; otherwise returns `null`.
     *
     * @param <T>         the content type of the data source
     * @param contentType the content type of the data source to be retrieved
     * @return the data source for the specified content type, or
     * `null` if this provider has no such data source
     * @throws NullPointerException  if the type is `null`
     * @throws ObjectClosedException if the provider is not open when this call is made
     * @since JWI 2.0.0
     */
    fun <T> getSource(contentType: IContentType<T>?): IDataSource<T>?
}
