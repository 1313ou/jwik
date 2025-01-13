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

import java.util.*
import java.util.regex.Pattern

/**
 * A unique identifier for an index word. An index word ID is sufficient to
 * retrieve a specific index word from the Wordnet database. It consists of both
 * a lemma (root form) and part of speech.
 *
 * Constructs an index word id object with the specified lemma and part of
 * speech. Since all index entries are in lower case, with whitespace
 * converted to underscores, this constructor applies this conversion.
 *
 * @param lemma the lemma for the id
 * @param pOS   the part of speech for the id
 * @throws IllegalArgumentException if the lemma is empty or all whitespace
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
class IndexWordID(
    lemma: String,
    override val pOS: POS,
) : IHasPOS, IItemID {

    /**
     * The lemma (root form) of the index word that this ID indicates.
     * The lemma will never be empty, or all whitespace.
     */
    val lemma: String = whitespace.matcher(lemma.lowercase()).replaceAll("_").trim { it <= ' ' }

    init {
        require(lemma.isNotEmpty())
    }

    override fun hashCode(): Int {
        return Objects.hash(lemma, pOS)
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (obj !is IndexWordID) {
            return false
        }
        val other = obj
        if (lemma != other.lemma) {
            return false
        }
        return pOS == other.pOS
    }

    override fun toString(): String {
        return "XID-$lemma-${pOS.tag}"
    }

    companion object {

        /**
         * Whitespace pattern for use in replacing whitespace with underscores
         *
         * @since JWI 2.1.2
         */
        private val whitespace: Pattern = Pattern.compile("\\s+")

        /**
         * Convenience method for transforming the result of the [.toString]
         * method into an `IndexWordID`
         *
         * @param value the string to be parsed
         * @return the index word id
         * @throws NullPointerException     if the specified string is null
         * @throws IllegalArgumentException if the specified string does not conform to an index word id
         * string
         * @since JWI 1.0
         */
        fun parseIndexWordID(value: String): IndexWordID {
            require(value.startsWith("XID-"))
            require(value[value.length - 2] == '-')

            val pos = POS.getPartOfSpeech(value[value.length - 1])
            return IndexWordID(value.substring(4, value.length - 2), pos)
        }
    }
}
