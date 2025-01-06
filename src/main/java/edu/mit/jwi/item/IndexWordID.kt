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

import edu.mit.jwi.Nullable
import java.util.*
import java.util.regex.Pattern

/**
 * Default implementation of `IIndexWordID`.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
class IndexWordID(lemma: String, @Nullable pos: POS) : IIndexWordID {

    override val lemma: String

    override val pOS: POS

    /**
     * Constructs an index word id object with the specified lemma and part of
     * speech. Since all index entries are in lower case, with whitespace
     * converted to underscores, this constructor applies this conversion.
     *
     * @param lemma the lemma for the id
     * @param pos   the part of speech for the id
     * @throws NullPointerException     if either argument is `null`
     * @throws IllegalArgumentException if the lemma is empty or all whitespace
     * @since JWI 1.0
     */
    init {
        var lemma = lemma
        if (pos == null) {
            throw NullPointerException()
        }
        lemma = lemma.lowercase(Locale.getDefault()).trim { it <= ' ' }
        require(lemma.isNotEmpty())
        this.lemma = whitespace.matcher(lemma).replaceAll("_")
        this.pOS = pos
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + lemma.hashCode()
        checkNotNull(this.pOS)
        result = prime * result + pOS.hashCode()
        return result
    }

    override fun equals(@Nullable obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (obj !is IIndexWordID) {
            return false
        }
        val other = obj
        if (lemma != other.lemma) {
            return false
        }
        checkNotNull(this.pOS)
        return this.pOS == other.pOS
    }

    override fun toString(): String {
        checkNotNull(this.pOS)
        return "XID-" + lemma + "-" + pOS.tag
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
         * @throws NullPointerException     if the specified string is `null`
         * @throws IllegalArgumentException if the specified string does not conform to an index word id
         * string
         * @since JWI 1.0
         */
        fun parseIndexWordID(@Nullable value: String): IndexWordID {
            if (value == null) {
                throw NullPointerException()
            }

            require(value.startsWith("XID-"))

            require(value[value.length - 2] == '-')

            val pos = POS.getPartOfSpeech(value[value.length - 1])
            return IndexWordID(value.substring(4, value.length - 2), pos!!)
        }
    }
}
