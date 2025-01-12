/* ******************************************************************************
 * Java Wordnet Interface Library (JWI) v2.4.0
 * Copyright (c) 2007-2015 Mark A. Finlayson
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 4.0
 * International Public License, which means it may be freely used for all
 * purposes, as long as proper acknowledgment is made.  See the license file
 * included with this distribution for more details.
 *******************************************************************************/
package edu.mit.jwi.item

import edu.mit.jwi.item.POS.Companion.getPartOfSpeech
import java.util.*

/**
 * Default implementation of `IExceptionEntryID`.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
class ExceptionEntryID(
    surfaceForm: String,
    pos: POS,
) : IExceptionEntryID {

    override val surfaceForm: String

    override val pOS: POS

    /**
     * Creates a new exception entry id with the specified information.
     *
     * @param surfaceForm the surface form for the entry
     * @param pos         the part of speech for the entry
     * @throws NullPointerException     if either argument is null
     * @throws IllegalArgumentException if the surface form is empty or all whitespace
     * @since JWI 1.0
     */
    init {
        var surfaceForm = surfaceForm
        if (pos == null) {
            throw NullPointerException()
        }
        if (surfaceForm == null) {
            throw NullPointerException()
        }
        surfaceForm = surfaceForm.trim { it <= ' ' }
        require(surfaceForm.isNotEmpty())
        // all exception entries are lower-case
        // this call also checks for null
        this.surfaceForm = surfaceForm.lowercase(Locale.getDefault())
        this.pOS = pos
    }

    override fun toString(): String {
        checkNotNull(this.pOS)
        return "EID-" + surfaceForm + "-" + pOS.tag
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + surfaceForm.hashCode()
        checkNotNull(this.pOS)
        result = prime * result + pOS.hashCode()
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (obj !is IExceptionEntryID) {
            return false
        }
        val other = obj
        if (surfaceForm != other.surfaceForm) {
            return false
        }
        checkNotNull(this.pOS)
        return this.pOS == other.pOS
    }

    companion object {

        /**
         * Convenience method for transforming the result of the [.toString]
         * method back into an `IExceptionEntryID`.
         *
         * @param value the string to parse
         * @return the derived exception entry id
         * @throws NullPointerException     if the specified string is null
         * @throws IllegalArgumentException if the specified string does not conform to an exception
         * entry id
         * @since JWI 2.2.0
         */
        fun parseExceptionEntryID(value: String): ExceptionEntryID {
            require(value.startsWith("EID-"))
            require(value[value.length - 2] == '-')

            val pos = getPartOfSpeech(value[value.length - 1])
            return ExceptionEntryID(value.substring(4, value.length - 2), pos)
        }
    }
}
