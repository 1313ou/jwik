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
 *
 *
 * A comparator that captures the ordering of lines in Wordnet exception files
 * (e.g., `exc.adv` or `adv.exc` files). These files are
 * ordered alphabetically.
 *
 *
 *
 * This class follows a singleton design pattern, and is not intended to be
 * instantiated directly; rather, call the [.getInstance] method to get
 * the singleton instance.
 *
 * This constructor is marked protected so that the class may be
 * sub-classed, but not directly instantiated. Obtain instances of this
 * class via the static [.getInstance] method.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
class ExceptionLineComparator private constructor() : ILineComparator {

    override fun compare(line1: String, line2: String): Int {
        val words1: Array<String?> = spacePattern.split(line1)
        val words2: Array<String?> = spacePattern.split(line2)

        if (words1.isEmpty()) {
            throw MisformattedLineException(line1)
        }
        if (words2.isEmpty()) {
            throw MisformattedLineException(line2)
        }
        return words1[0]!!.compareTo(words2[0]!!)
    }

    override val commentDetector: CommentComparator?
        get() = null

    companion object {

        /**
         * Returns the singleton instance of this class, instantiating it if
         * necessary. The singleton instance will not be null.
         *
         * @return the non-null singleton instance of this class,
         * instantiating it if necessary.
         * @since JWI 2.0.0
         */
        var instance: ExceptionLineComparator? = null
            get() {
                if (field == null) {
                    field = ExceptionLineComparator()
                }
                return field
            }
            private set

        // static fields
        private val spacePattern: Pattern = Pattern.compile(" ")
    }
}
