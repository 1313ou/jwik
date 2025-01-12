/* ******************************************************************************
 * Java Wordnet Interface Library (JWI) v2.4.0
 * Copyright (c) 2007-2015 Mark A. Finlayson
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 4.0
 * International Public License, which means it may be freely used for all
 * purposes, as long as proper acknowledgment is made.  See the license file
 * included with this distribution for more details.
 *******************************************************************************/
package edu.mit.jwi.data.compare

/**
 * A string comparator that may have an associated comment detector. The
 * `compare` method of this class will throw an [IllegalArgumentException] if the line data passed to that method is ill-formed.
 */
interface ILineComparator : Comparator<String> {

    val commentDetector: CommentProcessor?
}
