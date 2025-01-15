package edu.mit.jwi.item

import edu.mit.jwi.item.Word.Companion.checkWordNumber
import edu.mit.jwi.item.Word.Companion.zeroFillWordNumber
import java.util.*

/**
 * Base abstract class containing only reference to synset
 *
 * @property synsetID the synset id
 */
abstract class BaseWordID(override val synsetID: SynsetID) : IWordID {

    override val pOS: POS
        get() = synsetID.pOS

    override fun toString(): String {
        val pos = synsetID.pOS
        return "$WORDID_PREFIX${Synset.zeroFillOffset(synsetID.offset)}-${pos.tag.uppercaseChar()}"
    }

    companion object {

        private const val WORDID_PREFIX = "WID-"

        /**
         * Parses the result of the [.toString] method back into an `WordID`.
         * Word ids are always of the following format: WID-########-P-##-lemma where
         * ######## is the eight decimal digit zero-filled offset of the associated synset,
         * P is the upper case character representing the part of speech,
         * ## is the two hexadecimal digit zero-filled word number (or ?? if unknown), and
         * lemma is the lemma.
         *
         * @param value the string to be parsed
         * @return WordID the parsed id
         * @throws IllegalArgumentException if the specified string does not represent a word id
         */
        fun parseWordID(value: String): IWordID {
            require(value.length >= 19)
            require(value.startsWith("WID-"))

            // get synset id
            val offset = value.substring(4, 12).toInt()
            val pos = POS.getPartOfSpeech(value[13])
            val id = SynsetID(offset, pos)

            // get word number
            val numStr = value.substring(15, 17)
            if (numStr != WordLemmaID.UNKNOWN_NUMBER) {
                val num = numStr.toInt(16)
                return WordNumID(id, num)
            }

            // get lemma
            val lemma = value.substring(18)
            require(lemma != WordNumID.UNKNOWN_LEMMA)
            return WordLemmaID(id, lemma)
        }
    }
}

/**
 * Constructs a word id from synset id and word number
 * This constructor produces a word with a word number (but without a lemma)
 * The word number, which is a number from 1 to 255, indicates the order this word is listed in the Wordnet data files
 *
 * @return an integer between 1 and 255, inclusive
 *
 * @param synsetID the synset id
 * @property wordNumber the word number
 */
class WordNumID(synsetID: SynsetID, val wordNumber: Int) : BaseWordID(synsetID), IWordID {

    init {
        checkWordNumber(wordNumber)
    }

    override fun hashCode(): Int {
        return Objects.hash(synsetID, wordNumber)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null) {
            return false
        }
        if (other is BaseWordID && synsetID != other.synsetID)
            return false

        return when (other) {
            is WordLemmaNumID -> wordNumber == other.wordNumber
            is WordNumID      -> wordNumber == other.wordNumber
            is WordLemmaID    -> false
            else              -> false
        }
    }

    override fun toString(): String {
        return "${super.toString()}-${zeroFillWordNumber(wordNumber)}-$UNKNOWN_LEMMA"
    }

    companion object {

        const val UNKNOWN_LEMMA: String = "?"
    }
}

/**
 * Constructs a word id from synset id and lemma
 * This constructor produces a word id with a lemma
 * The lemma is a non-empty string non-whitespace string
 *
 * @param synsetID  the synset id
 * @param lemma lemma arg
 * @property lemma lemma
 * @throws IllegalArgumentException if the lemma is empty or all whitespace
 */
open class WordLemmaID(synsetID: SynsetID, lemma: String) : BaseWordID(synsetID), IWordID {

    val lemma: String = lemma.trim { it <= ' ' }

    init {
        require(lemma.isNotEmpty())
    }

    override fun hashCode(): Int {
        return Objects.hash(synsetID, lemma)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null) {
            return false
        }
        if (other is BaseWordID && synsetID != other.synsetID)
            return false

        return when (other) {
            is WordLemmaNumID -> lemma.equals(other.lemma, ignoreCase = true)
            is WordLemmaID    -> lemma.equals(other.lemma, ignoreCase = true)
            is WordNumID      -> false
            else              -> false
        }
    }

    override fun toString(): String {
        return "${super.toString()}-$UNKNOWN_NUMBER-$lemma"
    }

    companion object {

        const val UNKNOWN_NUMBER: String = "??"
    }
}

/**
 * Constructs a word id from the specified arguments.
 * This constructor produces a word id with a word number and a lemma
 * The word number, which is a number from 1 to 255, indicates the order this word is listed in the Wordnet data files
 * The lemma is a non-empty string non-whitespace string
 *
 * @param synsetID  the synset id
 * @property wordNumber the word number
 * @param lemma the lemma; may not be empty or all whitespace
 * @throws IllegalArgumentException if the lemma is empty or all whitespace
 */
class WordLemmaNumID(synsetID: SynsetID, val wordNumber: Int, lemma: String) : WordLemmaID(synsetID, lemma), IWordID {

    init {
        checkWordNumber(wordNumber)
    }

    override fun hashCode(): Int {
        return Objects.hash(synsetID, wordNumber, lemma)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null) {
            return false
        }
        if (other is BaseWordID && synsetID != other.synsetID)
            return false

        return when (other) {
            is WordLemmaNumID -> lemma.equals(other.lemma, ignoreCase = true) && wordNumber == other.wordNumber
            is WordLemmaID    -> lemma.equals(other.lemma, ignoreCase = true)
            is WordNumID      -> wordNumber == other.wordNumber
            else              -> false
        }
    }

    override fun toString(): String {
        return "${super.toString()}-${zeroFillWordNumber(wordNumber)}-$lemma"
    }
}
