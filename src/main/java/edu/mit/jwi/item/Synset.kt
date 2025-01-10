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
 * Default implementation of the `ISynset` interface.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
class Synset(
    id: ISynsetID,
    lexFile: ILexFile,
    isAdjSat: Boolean,
    isAdjHead: Boolean,
    gloss: String,
    wordBuilders: List<IWordBuilder>,
    ids: Map<IPointer, List<ISynsetID>>?,
) : ISynset {

    override val iD: ISynsetID

    override val gloss: String

    override val lexicalFile: ILexFile

    override val words: List<IWord>

    override val isAdjectiveSatellite: Boolean

    override val isAdjectiveHead: Boolean

    override val relatedSynsets: List<ISynsetID>

    override val relatedMap: Map<IPointer, List<ISynsetID>>

    override val offset: Int
        get() {
            checkNotNull(this.iD)
            return iD.offset
        }

    override val pOS: POS
        get() {
            checkNotNull(this.iD)
            return iD.pOS!!
        }

    override val type: Int
        get() {
            val pos = pOS
            if (pos != POS.ADJECTIVE) {
                checkNotNull(pos)
                return pos.number
            }
            return if (isAdjectiveSatellite) 5 else 3
        }

    /**
     * Constructs a new synset object with the specified parameters.
     *
     * @param id           the synset id; may not be `null`
     * @param lexFile      the lexical file for this synset; may not be `null`
     * @param isAdjSat     `true` if this object represents an adjective
     * satellite synset; `false` otherwise
     * @param isAdjHead    `true` if this object represents an adjective head
     * synset; `false` otherwise
     * @param gloss        the gloss for this synset; may not be `null`
     * @param wordBuilders the list of word builders for this synset; may not be
     * `null`
     * @param ids          a map of related synset lists, indexed by pointer; may be
     * `null`
     * @throws NullPointerException     if any of the id, lexical file, word list, or gloss are
     * `null`, or the word list contains a
     * `null`
     * @throws IllegalArgumentException if the word list is empty, or both the adjective satellite
     * and adjective head flags are set
     * @throws IllegalArgumentException if either the adjective satellite and adjective head flags
     * are set, and the lexical file number is not zero
     * @since JWI 1.0
     */
    init {
        require(!wordBuilders.isEmpty())
        require(!(isAdjSat && isAdjHead))
        require(!((isAdjSat || isAdjHead) && lexFile.number != 0))

        this.iD = id
        this.lexicalFile = lexFile
        this.gloss = gloss
        this.isAdjectiveSatellite = isAdjSat
        this.isAdjectiveHead = isAdjHead

        // words
        val words: MutableList<IWord?> = ArrayList<IWord?>(wordBuilders.size)
        for (wordBuilder in wordBuilders) {
            words.add(wordBuilder.toWord(this))
        }
        this.words = Collections.unmodifiableList<IWord?>(words)

        var hiddenSet: MutableSet<ISynsetID>? = null
        var hiddenMap: MutableMap<IPointer, List<ISynsetID>>? = null
        // fill synset map
        if (ids != null) {
            hiddenSet = LinkedHashSet<ISynsetID>()
            hiddenMap = HashMap<IPointer, List<ISynsetID>>(ids.size)
            for (entry in ids.entries) {
                if (entry.value == null || entry.value.isEmpty()) {
                    continue
                }
                hiddenMap.put(entry.key, Collections.unmodifiableList<ISynsetID?>(ArrayList<ISynsetID?>(entry.value)))
                hiddenSet.addAll(entry.value)
            }
        }
        this.relatedSynsets = if (hiddenSet != null && !hiddenSet.isEmpty()) Collections.unmodifiableList<ISynsetID?>(ArrayList<ISynsetID>(hiddenSet)) else listOf<ISynsetID>()
        this.relatedMap = if (hiddenMap != null && !hiddenMap.isEmpty()) Collections.unmodifiableMap<IPointer, List<ISynsetID>>(hiddenMap) else mapOf<IPointer, List<ISynsetID>>()
    }

    override fun getWord(wordNumber: Int): IWord {
        return words[wordNumber - 1]
    }

    override fun getRelatedSynsets(ptrType: IPointer): List<ISynsetID> {
        val result: List<ISynsetID>? = relatedMap[ptrType]
        return if (result != null) result else listOf<ISynsetID>()
    }

    override fun hashCode(): Int {
        val PRIME = 31
        var result = 1
        checkNotNull(gloss)
        result = PRIME * result + gloss.hashCode()
        result = PRIME * result + (if (this.isAdjectiveSatellite) 1231 else 1237)
        checkNotNull(this.iD)
        result = PRIME * result + iD.hashCode()
        result = PRIME * result + words.hashCode()
        result = PRIME * result + relatedMap.hashCode()
        return result
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
        checkNotNull(this.iD)
        if (this.iD != other.iD) {
            return false
        }
        if (words != other.words) {
            return false
        }
        checkNotNull(gloss)
        if (gloss != other.gloss) {
            return false
        }
        if (this.isAdjectiveSatellite != other.isAdjectiveSatellite) {
            return false
        }
        return relatedMap == other.relatedMap
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("SYNSET{")
        checkNotNull(this.iD)
        sb.append(this.iD)
        sb.append(" : Words[")
        for (word in words) {
            sb.append(word.toString())
            sb.append(", ")
        }
        sb.replace(sb.length - 2, sb.length, "]}")
        return sb.toString()
    }

    /**
     * A word builder used to construct word objects inside the synset object
     * constructor.
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

        /**
         * Adds the specified verb frame to this word.
         *
         * @param frame the frame to be added, may not be `null`
         * @throws NullPointerException if the specified frame is `null`
         * @since JWI 2.2.0
         */
        fun addVerbFrame(frame: IVerbFrame)

        /**
         * Adds a pointer from this word to another word with the specified id.
         *
         * @param ptrType the pointer type, may not be `null`
         * @param id      the word id, may not be `null`
         * @throws NullPointerException if either argument is `null`
         * @since JWI 2.2.0
         */
        fun addRelatedWord(ptrType: IPointer, id: IWordID)
    }

    /**
     * Holds information about word objects before they are instantiated.
     *
     * @author Mark A. Finlayson
     * @version 2.4.0
     * @since JWI 1.0
     */
    class WordBuilder

    /**
     * Constructs a new word builder object. The constructor does not check
     * its arguments - this is done when the word is created.
     *
     * @param num    the word number
     * @param lemma  the lemma
     * @param lexID  the id of the lexical file in which the word is listed
     * @param marker the adjective marker for the word
     * @since JWI 1.0
     */
        (
        private val num: Int,
        private val lemma: String,
        protected val lexID: Int,
        private val marker: AdjMarker?,
    ) : IWordBuilder {

        private val relatedWords: MutableMap<IPointer, MutableList<IWordID>> = HashMap<IPointer, MutableList<IWordID>>()

        private val verbFrames = ArrayList<IVerbFrame>()

        override fun addRelatedWord(ptrType: IPointer, id: IWordID) {
            if (ptrType == null) {
                throw NullPointerException()
            }
            if (id == null) {
                throw NullPointerException()
            }
            val words = relatedWords.computeIfAbsent(ptrType) { k: IPointer -> ArrayList<IWordID>() }
            words.add(id)
        }

        override fun addVerbFrame(frame: IVerbFrame) {
            if (frame == null) {
                throw NullPointerException()
            }
            verbFrames.add(frame)
        }

        override fun toWord(synset: ISynset): IWord {
            return Word(synset, num, lemma, lexID, marker, verbFrames, relatedWords)
        }
    }

    companion object {

        /**
         * This serial version UID identifies the last version of JWI whose
         * serialized instances of the Synset class are compatible with this
         * implementation.
         *
         * @since JWI 2.4.0
         */
        private const val serialVersionUID: Long = 240

        /**
         * Takes an integer in the closed range [0,99999999] and converts it into an
         * eight decimal digit zero-filled string. E.g., "1" becomes "00000001",
         * "1234" becomes "00001234", and so on. This is used for the generation of
         * synset and word numbers.
         *
         * @param offset the offset to be converted
         * @return the zero-filled string representation of the offset
         * @throws IllegalArgumentException if the specified offset is not in the valid range of
         * [0,99999999]
         * @since JWI 2.1.0
         */
        @JvmStatic

        fun zeroFillOffset(offset: Int): String {
            checkOffset(offset)
            val sb = StringBuilder(8)
            val offsetStr = offset.toString()
            val numZeros = 8 - offsetStr.length
            for (i in 0..<numZeros) {
                sb.append('0')
            }
            sb.append(offsetStr)
            return sb.toString()
        }

        /**
         * Throws an exception if the specified offset is not in the valid range of
         * [0,99999999].
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
         * @return `true` if the specified offset is in the closed range
         * [0, 99999999]; `false` otherwise.
         * @since JWI 2.2.0
         */
        fun isLegalOffset(offset: Int): Boolean {
            if (offset < 0) {
                return false
            }
            return offset <= 99999999
        }
    }
}
