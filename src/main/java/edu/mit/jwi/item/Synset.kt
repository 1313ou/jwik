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

import edu.mit.jwi.item.LexFile.Companion.ADJ_ALL
import edu.mit.jwi.item.Word.Companion.checkWordNumber
import java.util.*

/**
 * Default implementation of the `ISynset` interface.
 *
 * @property iD the synset id
 * @property lexicalFile the lexical file for this synset
 * @property isAdjectiveSatellite true if this object represents an adjective satellite synset; false otherwise
 * @property isAdjectiveHead true if this object represents an adjective head synset; false otherwise
 * @property gloss the gloss for this synset
 * @property words the list of words in this synset
 * @property related a map of related synset lists, indexed by pointer

 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
class Synset private constructor(
    override val iD: ISynsetID,
    override val lexicalFile: ILexFile,
    override val isAdjectiveSatellite: Boolean,
    override val isAdjectiveHead: Boolean,
    override val gloss: String,
    override val related: Map<IPointer, List<ISynsetID>>,
) : ISynset {

    override val offset: Int
        get() {
            return iD.offset
        }

    override val pOS: POS
        get() {
            return iD.pOS!!
        }

    override val type: Int
        get() {
            val pos = pOS
            if (pos == POS.ADJECTIVE) {
                return if (isAdjectiveSatellite) NUM_ADJECTIVE_SATELLITE else NUM_ADJECTIVE
            }
            return pos.number
        }

    override lateinit var words: List<IWord>

    /**
     * Default implementation of the `ISynset` interface.
     *
     * @param iD the synset id
     * @param lexicalFile the lexical file for this synset
     * @param isAdjectiveSatellite true if this object represents an adjective satellite synset; false otherwise
     * @param isAdjectiveHead true if this object represents an adjective head synset; false otherwise
     * @param gloss the gloss for this synset
     * @param wordBuilders the list of word builders for this synset
     * @param related a map of related synset lists, indexed by pointer
     */
    constructor(
        iD: ISynsetID,
        lexicalFile: ILexFile,
        isAdjectiveSatellite: Boolean,
        isAdjectiveHead: Boolean,
        gloss: String,
        wordBuilders: List<IWordBuilder>,
        related: Map<IPointer, List<ISynsetID>>?,
    ) : this(iD, lexicalFile, isAdjectiveSatellite, isAdjectiveHead, gloss, normalizeRelated(related)) {
        require(!wordBuilders.isEmpty())
        words = buildWords(wordBuilders, this)
    }

    /**
     * @throws IllegalArgumentException if the word list is empty, or both the adjective satellite and adjective head flags are set
     * @throws IllegalArgumentException if either the adjective satellite and adjective head flags are set, and the lexical file number is not zero
     * @since JWI 1.0
     */
    init {
        require(!(isAdjectiveSatellite && isAdjectiveHead))
        require(!((isAdjectiveSatellite || isAdjectiveHead) && lexicalFile.number != ADJ_ALL.number))
    }

    override fun hashCode(): Int {
        return Objects.hash(iD, words, related, gloss, isAdjectiveSatellite)
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (obj !is Synset) {
            return false
        }
        val other = obj
        if (iD != other.iD) {
            return false
        }
        if (words != other.words) {
            return false
        }
        if (gloss != other.gloss) {
            return false
        }
        if (isAdjectiveSatellite != other.isAdjectiveSatellite) {
            return false
        }
        return related == other.related
    }

    override fun toString(): String {
        return "SYNSET{${iD} : Words[${words.joinToString(separator = ", ")}]}"
    }

    /**
     * A word builder used to construct word objects inside the synset object constructor.
     *
     * @author Mark A. Finlayson
     * @version 2.4.0
     * @since JWI 2.2.0
     */
    interface IWordBuilder {

        /**
         * Creates the word represented by this builder. If the builder
         * represents invalid values for a word, this method may throw an
         * exception.
         *
         * @param synset the synset to which this word should be attached
         * @return the created word
         * @since JWI 2.2.0
         */

        fun toWord(synset: ISynset): IWord
    }

    /**
     * Holds information about word objects before they are instantiated.
     *
     * Constructs a new word builder object. The constructor does not check
     * its arguments - this is done when the word is created.
     *
     * @property number the word number
     * @property lemma the lemma
     * @property lexID the id of the lexical file in which the word is listed
     * @property marker the adjective marker for the word
     * @author Mark A. Finlayson
     * @version 2.4.0
     * @since JWI 1.0
     */
    data class WordBuilder(
        private val number: Int,
        private val lemma: String,
        private val lexID: Int,
        private val marker: AdjMarker?,
    ) : IWordBuilder {

        init {
            checkWordNumber(number)
        }

        private val relatedWords: MutableMap<IPointer, MutableList<IWordID>> = HashMap<IPointer, MutableList<IWordID>>()

        private val verbFrames = ArrayList<IVerbFrame>()

        override fun toWord(synset: ISynset): IWord {
            return Word(synset, WordLemmaNumID(synset.iD, number, lemma), lexID, marker, verbFrames, relatedWords)
        }

        fun addRelatedWord(ptrType: IPointer, id: IWordID) {
            val words = relatedWords.computeIfAbsent(ptrType) { k: IPointer -> ArrayList<IWordID>() }
            words.add(id)
        }

        fun addVerbFrame(frame: IVerbFrame) {
            verbFrames.add(frame)
        }
    }

    companion object {

        /**
         * Takes an integer in the closed range [0,99999999] and converts it into an eight decimal digit zero-filled string.
         * E.g., "1" becomes "00000001", "1234" becomes "00001234", and so on.
         * This is used for the generation of synset and word numbers.
         *
         * @param offset the offset to be converted
         * @return the zero-filled string representation of the offset
         * @throws IllegalArgumentException if the specified offset is not in the valid range of [0,99999999]
         * @since JWI 2.1.0
         */
        @JvmStatic
        fun zeroFillOffset(offset: Int): String {
            checkOffset(offset)
            return "%08d".format(offset)
        }

        /**
         * Throws an exception if the specified offset is not in the valid range of [0,99999999].
         *
         * @param offset the offset to be checked
         * @return the checked offset
         * @throws IllegalArgumentException if the specified offset is not in the valid range of
         * [0,99999999]
         * @since JWI 2.1.0
         */
        @JvmStatic
        fun checkOffset(offset: Int): Int {
            require(isLegalOffset(offset)) { "'$offset' is not a valid offset; offsets must be in the closed range [0,99999999]" }
            return offset
        }

        /**
         * Returns true an exception if the specified offset is not in the valid
         * range of [0,99999999].
         *
         * @param offset the offset to be checked
         * @return true if the specified offset is in the closed range [0, 99999999]; false otherwise.
         * @since JWI 2.2.0
         */
        fun isLegalOffset(offset: Int): Boolean {
            if (offset < 0)
                return false
            return offset <= 99999999
        }

        fun buildWords(wordBuilders: List<IWordBuilder>, synset: ISynset): List<IWord> {
            return wordBuilders
                .map { it.toWord(synset) }
                .toList()
        }

        private fun normalizeRelated(related: Map<IPointer, List<ISynsetID>>?): Map<IPointer, List<ISynsetID>> {
            return related?.entries
                ?.filterNot { it.value.isEmpty() }
                ?.associate { it.key to it.value }
                ?: emptyMap()
        }
    }
}
