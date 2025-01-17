package edu.mit.jwi.item

import java.util.*

/**
 * A unique identifier for a synset, sufficient to retrieve it from the Wordnet database.
 * It consists of a part-of-speech and an offset.
 *
 * @param offset the offset
 * @param pOS the part-of-speech
 * @throws IllegalArgumentException if the specified offset is not a legal offset
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
         * String prefix for the toString method.
         */
        const val SYNSETID_PREFIX: String = "SID-"

        /**
         * Convenience method for transforming the result of the toString method back into an ISynsetID.
         * Synset IDs are always 14 characters long and have the following format:
         * SID-########-C, where
         * ######## is the zero-filled eight decimal digit offset of the synset, and
         * C is the upper-case character code indicating the part-of-speech.
         *
         * @param value the string representation of the id; may include leading or trailing whitespace
         * @return a synset id object corresponding to the specified string representation
         * @throws IllegalArgumentException if the specified string is not a properly formatted synset id
         */
        fun parseSynsetID(value: String): SynsetID {
            var value = value.trim { it <= ' ' }
            require(value.length == 14)
            require(value.startsWith("SID-"))

            // get offset
            val offset = value.substring(4, 12).toInt()

            // get pos
            val posTag = value[13].lowercaseChar()
            val pos = POS.getPartOfSpeech(posTag)
            requireNotNull(pos) { "unknown part-of-speech tag: $posTag" }
            return SynsetID(offset, pos)
        }
    }
}
