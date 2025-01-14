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

import edu.mit.jwi.data.parse.MisformattedLineException
import java.util.regex.Pattern

/**
 * A comparator that captures the ordering of lines in Wordnet exception files (e.g., `exc.adv` or `adv.exc` files).
 * These files are ordered alphabetically.
 */
object ExceptionLineComparator : ILineComparator {

    override val commentProcessor: CommentProcessor? = null

    override fun compare(line1: String, line2: String): Int {
        val words1 = SEPARATOR.split(line1)
        if (words1.isEmpty()) {
            throw MisformattedLineException(line1)
        }
        val words2 = SEPARATOR.split(line2)
        if (words2.isEmpty()) {
            throw MisformattedLineException(line2)
        }
        return words1[0].compareTo(words2[0])
    }

    private val SEPARATOR: Pattern = Pattern.compile(" ")
}
