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

import edu.mit.jwi.item.WordLemmaID.Companion.UNKNOWN_NUMBER
import java.util.*

/**
 * A word, which in Wordnet is an index word paired with a synset.
 *
 * Constructs a new word object.
 *
 * @param synset the synset for the word
 * @param iD the word id; its word lemma may not be empty or all whitespace
 * @param lexicalID the lexical id
 * @param adjMarker non-null only if this is an adjective
 * @param verbFrames verb frames if this is a verb
 * @param related lexical pointers
 * @throws IllegalArgumentException if the adjective marker is non-null and this is not an adjective
 * @since JWI 1.0

 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
class Word(
    val synset: Synset,

    override val iD: WordLemmaID,

    /**
     * An integer in the closed range [0,15] that, when appended onto lemma,
     * uniquely identifies a sense within a lexicographer file. Lexical id
     * numbers usually start with 0, and are incremented as additional senses of
     * the word are added to the same file, although there is no requirement
     * that the numbers be consecutive or begin with 0. Note that a value of 0
     * is the default, and therefore is not present in lexicographer files. In
     * the wordnet data files the lexical id is represented as a one digit
     * hexadecimal integer.
     *
     * @return the lexical id of the word, an integer between 0 and 15,
     * inclusive
     * @since JWI 1.0
     */
    val lexicalID: Int,

    private val adjMarker: AdjMarker?,

    verbFrames: List<IVerbFrame>?,

    related: Map<Pointer, List<IWordID>>,
) : IHasPOS, IItem<IWordID> {

    override val pOS: POS
        get() = iD.synsetID.pOS!!

    val senseKey: ISenseKey = SenseKey(iD.lemma, lexicalID, synset)

    val verbFrames: List<IVerbFrame> = if (verbFrames == null || verbFrames.isEmpty()) emptyList() else verbFrames

    val related: Map<Pointer, List<IWordID>> = normalizeRelated(related)

    val relatedWords: List<IWordID>
        get() = related.values
            .flatMap { it.toList() }
            .distinct()
            .toList()

    val lemma: String
        get() = iD.lemma

    val adjectiveMarker: AdjMarker?
        get() = adjMarker

    init {
        checkLexicalID(lexicalID)
        require(!(synset.pOS !== POS.ADJECTIVE && adjMarker != null))
    }

    override fun toString(): String {
        val sid = iD.synsetID.toString().substring(4)
        return when (iD) {
            is WordLemmaNumID -> "W-$sid-${iD.wordNumber}-${iD.lemma}"
            else              -> "W-$sid-$UNKNOWN_NUMBER-${iD.lemma}"
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(iD, lexicalID, adjMarker, related, verbFrames)
    }

    override fun equals(obj: Any?): Boolean {
        // check nulls
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }

        // check interface
        if (obj !is Word) {
            return false
        }
        val that = obj

        // check id
        checkNotNull(this.iD)
        if (iD != that.iD) {
            return false
        }

        // check lexical id
        if (lexicalID != that.lexicalID) {
            return false
        }

        // check adjective marker
        if (adjMarker == null) {
            if (that.adjMarker != null) {
                return false
            }
        } else if (adjMarker != that.adjMarker) {
            return false
        }

        // check maps
        if (verbFrames != that.verbFrames) {
            return false
        }
        return related == that.related
    }


    /**
     * Returns an immutable list of all word ids related to this word by the
     * specified pointer type. Note that this only returns words related by
     * lexical pointers (i.e., not semantic pointers). To retrieve items related
     * by semantic pointers, call [Synset.getRelatedFor]. If this
     * word has no targets for the specified pointer, this method
     * returns an empty list. This method never returns null.
     *
     * @param ptr the pointer for which related words are requested
     * @return the list of words related by the specified pointer, or an empty
     * list if none.
     * @since JWI 2.0.0
     */
    fun getRelatedFor(ptr: Pointer): List<IWordID> {
        return related[ptr] ?: emptyList<IWordID>()
    }

    companion object {

        /**
         * Checks the specified word number, and throws an
         * [IllegalArgumentException] if it is not legal.
         *
         * @param num the number to check
         * @throws IllegalArgumentException if the specified lexical id is not in the closed range [0,15]
         * @since JWI 2.1.0
         */
        @JvmStatic
        fun checkWordNumber(num: Int) {
            require(!isIllegalWordNumber(num)) { "'$num is an illegal word number: word numbers are in the closed range [1,255]" }
        }

        /**
         * Flag to check lexical IDs. Determines if lexical IDs are checked to be in the closed range [0,15]
         */
        var checkLexicalId: Boolean = false

        /**
         * Checks the specified lexical id, and throws an
         * [IllegalArgumentException] if it is not legal.
         *
         * @param id the id to check
         * @throws IllegalArgumentException if the specified lexical id is not in the closed range [0,15]
         * @since JWI 2.1.0
         */
        @JvmStatic
        fun checkLexicalID(id: Int) {
            require(!(checkLexicalId && isIllegalLexicalID(id))) { "'$id is an illegal lexical id: lexical ids are in the closed range [0,15]" }
        }

        /**
         * Lexical ids are always an integer in the closed range [0,15]. In the
         * wordnet data files, lexical ids are represented as a one digit
         * hexadecimal integer.
         *
         * @param id the lexical id to check
         * @return true if the specified integer is an invalid lexical
         * id; false otherwise.
         * @since JWI 2.1.0
         */
        fun isIllegalLexicalID(id: Int): Boolean {
            if (id < 0) {
                return true
            }
            return id > 15
        }

        /**
         * Word numbers are always an integer in the closed range [1,255]. In the
         * wordnet data files, the word number is determined by the order of the
         * word listing.
         *
         * @param num the number to check
         * @return true if the specified integer is an invalid lexical
         * id; false otherwise.
         * @since JWI 2.1.0
         */
        fun isIllegalWordNumber(num: Int): Boolean {
            if (num < 1) {
                return true
            }
            return num > 255
        }

        /**
         * Returns a string form of the lexical id as they are written in data
         * files, which is a single digit hex number.
         *
         * @param lexID the lexical id to convert
         * @return a string form of the lexical id as they are written in data
         * files, which is a single digit hex number.
         * @throws IllegalArgumentException if the specified integer is not a valid lexical id.
         * @since JWI 2.1.0
         */

        fun getLexicalIDForDataFile(lexID: Int): String {
            checkLexicalID(lexID)
            return Integer.toHexString(lexID)
        }

        // static cache
        private val lexIDNumStrs = arrayOf<String>("00", "01", "02", "03", "04", "05", "06", "07", "08", "09")

        /**
         * Returns a string form of the lexical id as they are written in sense
         * keys, which is as a two-digit decimal number.
         *
         * @param lexID the lexical id to convert
         * @return a string form of the lexical id as they are written in sense
         * keys, which is as a two-digit decimal number.
         * @throws IllegalArgumentException if the specified integer is not a valid lexical id.
         * @since JWI 2.1.0
         */
        @JvmStatic

        fun getLexicalIDForSenseKey(lexID: Int): String {
            checkLexicalID(lexID)
            return if (lexID < 10) lexIDNumStrs[lexID] else lexID.toString()
        }

        /**
         * Returns a string representation of the specified integer as a two hex digit zero-filled string.
         * E.g., "1" becomes "01", "10" becomes "0A", and so on.
         * This is used for the generation of Word ID numbers.
         *
         * @param num the number to be converted
         * @return a two hex digit zero-filled string representing the specified number
         * @throws IllegalArgumentException if the specified number is not a legal word number
         * @since JWI 2.1.0
         */
        @JvmStatic

        fun zeroFillWordNumber(num: Int): String {
            return "%02x".format(num)
        }

        private fun normalizeRelated(related: Map<Pointer, List<IWordID>>?): Map<Pointer, List<IWordID>> {
            return related?.entries
                ?.filterNot { it.value.isEmpty() }
                ?.associate { it.key to it.value }
                ?: emptyMap()
        }
    }
}
