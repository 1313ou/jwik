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
 * @param pOS the part of speech for the id
 * @throws IllegalArgumentException if the lemma is empty or all whitespace
 */
class IndexWordID(
    lemma: String,
    override val pOS: POS,
) : IHasPOS, IItemID {

    /**
     * The lemma (root form) of the index word that this ID indicates.
     * The lemma will never be empty, or all whitespace.
     */
    val lemma: String = lemma.asIndexWordLemma()

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
         * Convenience method for transforming the result of the toString method into an `IndexWordID`
         *
         * @param value the string to be parsed
         * @return the index word id
         * @throws IllegalArgumentException if the specified string does not conform to an index word id string
         */
        fun parseIndexWordID(value: String): IndexWordID {
            require(value.startsWith("XID-"))
            require(value[value.length - 2] == '-')

            val pos = POS.getPartOfSpeech(value[value.length - 1])
            return IndexWordID(value.substring(4, value.length - 2), pos)
        }
    }
}
