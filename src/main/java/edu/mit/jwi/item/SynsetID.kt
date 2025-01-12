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

/**
 * A unique identifier for a synset,
 * sufficient to retrieve it from the Wordnet database. It consists of a
 * part of speech and an offset.
 *
 * @param offset the offset
 * @param pOS the part of speech; may not be null
 * @throws IllegalArgumentException if the specified offset is not a legal offset
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
class SynsetID(
    /**
     * The byte offset for the synset.
     */
    val offset: Int,

    /**
     * The Part Of Speech
     */
    override val pOS: POS,
) : IHasPOS, IItemID {

    init {
        Synset.checkOffset(offset)
    }

    override fun hashCode(): Int {
        return Objects.hash(offset, pOS)
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (obj !is SynsetID) {
            return false
        }
        val other = obj
        if (offset != other.offset) {
            return false
        }
        return pOS == other.pOS
    }

    override fun toString(): String {
        return "$SYNSETID_PREFIX${Synset.zeroFillOffset(offset)}-${pOS.tag.uppercaseChar()}"
    }

    companion object {

        /**
         * String prefix for the [.toString] method.
         *
         * @since JWI 2.0.0
         */
        const val SYNSETID_PREFIX: String = "SID-"

        /**
         * Convenience method for transforming the result of the [.toString]
         * method back into an `ISynsetID`. Synset IDs are always 14
         * characters long and have the following format: SID-########-C, where
         * ######## is the zero-filled eight decimal digit offset of the synset, and
         * C is the upper-case character code indicating the part of speech.
         *
         * @param value the string representation of the id; may include leading or
         * trailing whitespace
         * @return a synset id object corresponding to the specified string
         * representation
         * @throws NullPointerException     if the specified string is null
         * @throws IllegalArgumentException if the specified string is not a properly formatted synset id
         * @since JWI 1.0
         */

        fun parseSynsetID(value: String): SynsetID {
            var value = value.trim { it <= ' ' }
            require(value.length == 14)
            require(value.startsWith("SID-"))

            // get offset
            val offset = value.substring(4, 12).toInt()

            // get pos
            val tag = value[13].lowercaseChar()
            val pos = POS.getPartOfSpeech(tag)
            requireNotNull(pos) { "unknown part of speech tag: $tag" }
            return SynsetID(offset, pos)
        }
    }
}
