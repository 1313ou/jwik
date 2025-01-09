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
 * A detector for comment lines in data resources. Objects that implement this
 * interface also serve as comparators that say how comment lines are ordered,
 * if at all.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
interface ICommentDetector : Comparator<String> {

    /**
     * Returns `true` if the specified string is a comment line,
     * `false` otherwise.
     *
     * @param line the line to be analyzed
     * @return `true` if the specified string is a comment line,
     * `false` otherwise.
     * @throws NullPointerException if the specified line is `null`
     * @since JWI 1.0
     */
    fun isCommentLine(line: String): Boolean
}