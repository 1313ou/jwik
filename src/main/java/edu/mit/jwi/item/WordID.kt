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
        return "$WORDID_PREFIX ${Synset.zeroFillOffset(synsetID.offset)}-${pos.tag.uppercaseChar()}"
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
         * @since JWI 1.0
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

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as WordNumID
        if (synsetID != other.synsetID) {
            return false
        }
        if (other.wordNumber != -1 && wordNumber != -1 && other.wordNumber != wordNumber) {
            return false
        }
        return true
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

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as WordLemmaID
        if (synsetID != other.synsetID) {
            return false
        }
        return other.lemma.equals(lemma, ignoreCase = true)
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
 * @param synsetID  the synset id; may not be null
 * @property wordNumber the word number
 * @param lemma the lemma; may not be empty or all whitespace
 * @throws IllegalArgumentException if the lemma is empty or all whitespace
 * @since JWI 1.0
 */
class WordLemmaNumID(synsetID: SynsetID, val wordNumber: Int, lemma: String) : WordLemmaID(synsetID, lemma), IWordID {

    init {
        checkWordNumber(wordNumber)
    }

    override fun hashCode(): Int {
        return Objects.hash(synsetID, wordNumber, lemma)
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as WordLemmaNumID
        if (synsetID != other.synsetID) {
            return false
        }
        if (other.wordNumber != -1 && wordNumber != -1 && other.wordNumber != wordNumber) {
            return false
        }
        return other.lemma.equals(lemma, ignoreCase = true)
    }

    override fun toString(): String {
        return "${super.toString()}-${zeroFillWordNumber(wordNumber)}-$lemma"
    }
}
