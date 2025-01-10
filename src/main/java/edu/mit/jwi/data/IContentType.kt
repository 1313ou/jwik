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

import edu.mit.jwi.data.compare.ILineComparator
import edu.mit.jwi.item.IHasPOS

/**
 * Objects that implement this interface represent all possible types of content
 * that are contained in the dictionary data resources. Each unique object of
 * this type will correspond to a particular resource or file.
 *
 * In the standard Wordnet distributions, examples of content types would
 * include, but would not be limited to, *Index*, *Data*, and
 * *Exception* files for each part of speech.
 *
 *
 * @param <T> the parameterization of the data type for this content type
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
interface IContentType<T> : IHasPOS, IHasCharset {

    val key: ContentTypeKey

    val dataType: IDataType<T>

    /**
     * Returns a comparator that can be used to determine ordering between
     * different lines of data in the resource. This is used for searching. If
     * the data in the resource is not ordered, then this method returns
     * `null`.
     *
     * @return a comparator that imposes an ordering on the lines in the data
     * file; or `null` if there is no comparator
     * @since JWI 1.0
     */
     val lineComparator: ILineComparator?
}
