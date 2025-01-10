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

import edu.mit.jwi.NonNull
import edu.mit.jwi.Nullable
import java.util.*

/**
 * Default implementation of the `IWord` interface.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
class Word(
    synset: ISynset,
    id: IWordID,
    lexID: Int,
    adjMarker: AdjMarker?,
    frames: List<IVerbFrame>?,
    pointers: Map<IPointer, List<IWordID>>,
) : IWord {

    override val iD: IWordID
    override val synset: ISynset
    override val senseKey: ISenseKey
    override val lexicalID: Int
    override val verbFrames: List<IVerbFrame>
    override val relatedWords: List<IWordID>
    override val relatedMap: Map<IPointer, List<IWordID>>
    private val adjMarker: AdjMarker?

    /**
     * Constructs a new word object.
     *
     * @param synset    the synset for the word; may not be `null`
     * @param number    the word number
     * @param lemma     the word lemma; may not be empty or all whitespace
     * @param lexID     the lexical id
     * @param adjMarker non-`null` only if this is an adjective
     * @param frames    verb frames if this is a verb
     * @param pointers  lexical pointers
     * @throws NullPointerException     if the synset is `null`
     * @throws IllegalArgumentException if the adjective marker is non-`null` and this is not an adjective
     * @since JWI 1.0
     */
    constructor(
        synset: ISynset,
        number: Int,
        lemma: String,
        lexID: Int,
        adjMarker: AdjMarker?,
        frames: List<IVerbFrame>?,
        pointers: Map<IPointer, List<IWordID>>,
    ) : this(synset, WordID(synset.iD, number, lemma), lexID, adjMarker, frames, pointers)

    override val lemma: String
        get() {
            checkNotNull(this.iD)
            return iD.lemma!!
        }

    override val pOS: POS
        get() {
            val sid = iD.synsetID
            return sid.pOS!!
        }

    override val adjectiveMarker: AdjMarker?
        get() = adjMarker

        override fun getRelatedWords(ptrType: IPointer): List<IWordID> {
        val result: List<IWordID>? = relatedMap.get(ptrType)
        return result ?: emptyList<IWordID>()
    }

    override fun toString(): String {
        checkNotNull(this.iD)
        val sid = checkNotNull(iD.synsetID)
        if (iD.wordNumber == 0) {
            return "W-" + sid.toString().substring(4) + "-?-" + iD.lemma
        } else {
            return "W-" + sid.toString().substring(4) + "-" + iD.wordNumber + "-" + iD.lemma
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    override fun hashCode(): Int {
        val PRIME = 31
        var result: Int
        result = PRIME + verbFrames.hashCode()
        result = PRIME * result + relatedMap.hashCode()
        checkNotNull(this.iD)
        result = PRIME * result + iD.hashCode()
        result = PRIME * result + this.lexicalID
        result = PRIME * result + (if (adjMarker == null) 0 else adjMarker.hashCode())
        return result
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
        if (this.iD != that.iD) {
            return false
        }

        // check lexical id
        if (this.lexicalID != that.lexicalID) {
            return false
        }

        // check adjective marker
        if (this.adjMarker == null) {
            if (that.adjMarker != null) {
                return false
            }
        } else if (adjMarker != that.adjMarker) {
            return false
        }

        // check maps
        if (this.verbFrames != that.verbFrames) {
            return false
        }
        return this.relatedMap == that.relatedMap
    }

    /**
     * Constructs a new word object.
     *
     * @param synset    the synset for the word; may not be `null` the word
     * lemma; may not be empty or all whitespace
     * @param id        the word id; may not be `null`
     * @param lexID     the lexical id
     * @param adjMarker non-`null` only if this is an adjective
     * @param frames    verb frames if this is a verb
     * @param pointers  lexical pointers
     * @throws NullPointerException     if the synset or word ID is `null`
     * @throws IllegalArgumentException if the adjective marker is non-`null` and this is
     * not an adjective
     * @since JWI 1.0
     */
    init {
        // check arguments
        if (synset == null) {
            throw NullPointerException()
        }
        if (id == null) {
            throw NullPointerException()
        }
        checkLexicalID(lexID)
        require(!(synset.pOS !== POS.ADJECTIVE && adjMarker != null))

        // fill synset map
        var hiddenSet: Set<IWordID>? = null
        var hiddenMap: Map<IPointer, MutableList<IWordID>>? = null
        if (pointers != null) {
            hiddenSet = LinkedHashSet<IWordID>()
            hiddenMap = HashMap<IPointer, MutableList<IWordID>>(pointers.size)
            for (entry in pointers.entries) {
                if (entry.value != null && !entry.value.isEmpty()) {
                    hiddenMap.put(entry.key, Collections.unmodifiableList<IWordID?>(ArrayList<IWordID?>(entry.value)))
                    hiddenSet.addAll(entry.value)
                }
            }
        }

        // field assignments
        this.synset = synset
        this.iD = id
        this.lexicalID = lexID
        this.adjMarker = adjMarker
        val lemma = checkNotNull(id.lemma)
        this.senseKey = SenseKey(lemma, lexID, synset)
        this.relatedWords = if (hiddenSet != null && !hiddenSet.isEmpty()) Collections.unmodifiableList<IWordID>(ArrayList<IWordID>(hiddenSet)) else listOf<IWordID>()
        this.relatedMap = if (hiddenMap != null && !hiddenMap.isEmpty()) Collections.unmodifiableMap<IPointer, List<IWordID>>(hiddenMap) else mapOf<IPointer, List<IWordID>>()
        this.verbFrames = if (frames == null || frames.isEmpty()) listOf<IVerbFrame>() else Collections.unmodifiableList<IVerbFrame>(ArrayList<IVerbFrame>(frames))
    }

    companion object {

        /**
         * This serial version UID identifies the last version of JWI whose
         * serialized instances of the Word class are compatible with this
         * implementation.
         *
         * @since JWI 2.4.0
         */
        private const val serialVersionUID: Long = 240

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
         * Get flag to check lexical IDs.
         *
         * @return whether lexical IDs are checked to be in the closed range [0,15].
         */
        /**
         * Set flag to check lexical IDs.
         *
         * @param flag whether lexical IDs are checked to be in the closed range [0,15].
         */
        /**
         * Determines if lexical IDs are checked to be in the closed range [0,15]
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
         * @return `true` if the specified integer is an invalid lexical
         * id; `false` otherwise.
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
         * @return `true` if the specified integer is an invalid lexical
         * id; `false` otherwise.
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
        @NonNull
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
        @NonNull
        fun getLexicalIDForSenseKey(lexID: Int): String {
            checkLexicalID(lexID)
            return if (lexID < 10) lexIDNumStrs[lexID] else lexID.toString()
        }

        /**
         * Returns a string representation of the specified integer as a two hex
         * digit zero-filled string. E.g., "1" becomes "01", "10" becomes "0A", and
         * so on. This is used for the generation of Word ID numbers.
         *
         * @param num the number to be converted
         * @return a two hex digit zero-filled string representing the specified
         * number
         * @throws IllegalArgumentException if the specified number is not a legal word number
         * @since JWI 2.1.0
         */
        @JvmStatic
        @NonNull
        fun zeroFillWordNumber(num: Int): String {
            checkWordNumber(num)
            val sb = StringBuilder(2)
            val str = Integer.toHexString(num)
            val numZeros = 2 - str.length
            for (i in 0..<numZeros) {
                sb.append('0')
            }
            for (i in 0..<str.length) {
                sb.append(str.get(i).uppercaseChar())
            }
            return sb.toString()
        }
    }
}
