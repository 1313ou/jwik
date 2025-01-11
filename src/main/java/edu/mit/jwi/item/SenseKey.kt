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

import edu.mit.jwi.item.Word.Companion.checkLexicalID
import edu.mit.jwi.item.Word.Companion.getLexicalIDForSenseKey

/**
 * Concrete, default implementation of the `ISenseKey` interface.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.1.0
 */
class SenseKey(
    lemma: String,
    lexID: Int,
    pos: POS,
    isAdjSat: Boolean,
    lexFile: ILexFile,
) : ISenseKey {

    override val lemma: String

    override val lexicalID: Int

    override val pOS: POS

    override val isAdjectiveSatellite: Boolean

    override val lexicalFile: ILexFile

    override val synsetType: Int
        get() {
            checkNotNull(this.pOS)
            return if (this.isAdjectiveSatellite) 5 else pOS.number
        }

    override val headWord: String?
        get() {
            checkHeadSet()
            return headLemma
        }

    override val headID: Int
        get() {
            checkHeadSet()
            return headLexID
        }

    // dynamic fields
    private var isHeadSet: Boolean

    private var headLemma: String? = null

    private var headLexID = -1

    private var toString: String? = null

    /**
     * Constructs a new sense key.
     *
     * @param lemma  the lemma for the sense key
     * @param lexID  the lexical id of the sense key
     * @param synset the synset for the sense key
     * @throws NullPointerException if either the lemma or synset is null
     * @since JWI 2.1.0
     */
    constructor(lemma: String, lexID: Int, synset: ISynset) : this(lemma, lexID, synset.pOS!!, synset.isAdjectiveSatellite, synset.lexicalFile)

    /**
     * Constructs a new sense key.
     *
     * @param lemma       the lemma; may not be null
     * @param lexID       the lexical id
     * @param pos         the part of speech; may not be null
     * @param isAdjSat    true if this represents an adjective satellite;
     * false otherwise
     * @param lexFile     the lexical file; may not be null
     * @param originalKey the original key string
     * @throws NullPointerException if the lemma, lexical file, or original key is
     * null
     * @since JWI 2.1.0
     */
    constructor(lemma: String, lexID: Int, pos: POS, isAdjSat: Boolean, lexFile: ILexFile, originalKey: String) : this(lemma, lexID, pos, isAdjSat, lexFile) {
        if (originalKey == null) {
            throw NullPointerException()
        }
        this.toString = originalKey
    }

    /**
     * Constructs a new sense key.
     *
     * @param lemma       the lemma; may not be null
     * @param lexID       the lexical id
     * @param pos         the part of speech; may not be null
     * @param lexFile     the lexical file; may not be null
     * @param originalKey the original key string
     * @param headLemma   the head lemma
     * @param headLexID   the head lexical id; ignored if head lemma is null
     * @throws NullPointerException if the lemma, lexical file, or original key is
     * null
     * @since JWI 2.1.0
     */
    constructor(lemma: String, lexID: Int, pos: POS, lexFile: ILexFile, headLemma: String?, headLexID: Int, originalKey: String) : this(lemma, lexID, pos, (headLemma != null), lexFile) {
        if (headLemma == null) {
            isHeadSet = true
        } else {
            setHead(headLemma, headLexID)
        }
        if (originalKey == null) {
            throw NullPointerException()
        }
        this.toString = originalKey
    }

    /**
     * Constructs a new sense key.
     *
     * @param lemma    the lemma; may not be null
     * @param lexID    the lexical id
     * @param pos      the part of speech; may not be null
     * @param isAdjSat true if this is an adjective satellite sense key;
     * false otherwise
     * @param lexFile  the lexical file; may not be null
     * @throws NullPointerException if the lemma, part of speech, or lexical file is
     * null
     * @since JWI 2.1.0
     */
    init {
        if (pos == null) {
            throw NullPointerException()
        }
        if (lexFile == null) {
            throw NullPointerException()
        }

        // all sense key lemmas need not be in lower case
        // also checks for null
        this.lemma = lemma //.toLowerCase();
        this.lexicalID = lexID
        this.pOS = pos
        this.isAdjectiveSatellite = isAdjSat
        this.lexicalFile = lexFile
        this.isHeadSet = !isAdjSat
    }

    override fun setHead(headLemma: String, headLexID: Int) {
        check(needsHeadSet())
        checkLexicalID(headLexID)
        require(headLemma.trim { it <= ' ' }.isNotEmpty())
        this.headLemma = headLemma
        this.headLexID = headLexID
        this.isHeadSet = true
    }

    override fun needsHeadSet(): Boolean {
        return !isHeadSet
    }

    override fun compareTo(key: ISenseKey): Int {

        // first sort alphabetically by lemma
        var cmp: Int = this.lemma.compareTo(key.lemma)
        if (cmp != 0) {
            return cmp
        }

        // then sort by synset type
        cmp = this.synsetType.toFloat().compareTo(key.synsetType.toFloat())
        if (cmp != 0) {
            return cmp
        }

        // then sort by lex_filenum
        val lf = checkNotNull(this.lexicalFile)
        val lf2: ILexFile? = checkNotNull(key.lexicalFile)
        cmp = lf.number.toFloat().compareTo(lf2!!.number.toFloat())
        if (cmp != 0) {
            return cmp
        }

        // then sort by lex_id
        cmp = this.lexicalID.toFloat().compareTo(key.lexicalID.toFloat())
        if (cmp != 0) {
            return cmp
        }

        if (!this.isAdjectiveSatellite && !key.isAdjectiveSatellite) {
            return 0
        }
        if (!this.isAdjectiveSatellite and key.isAdjectiveSatellite) {
            return -1
        }
        if (this.isAdjectiveSatellite and !key.isAdjectiveSatellite) {
            return 1
        }

        // then sort by head_word
        val hw: String? = checkNotNull(this.headWord)
        val hw2: String? = checkNotNull(key.headWord)
        cmp = hw!!.compareTo(hw2!!)
        if (cmp != 0) {
            return cmp
        }

        // finally by head_id
        return this.headID.toFloat().compareTo(key.headID.toFloat())
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */

    override fun toString(): String {
        checkHeadSet()
        if (toString == null) {
            toString = toString(this)
        }
        return toString!!
    }

    /**
     * Throws an exception if the head is not yet set.
     *
     * @throws IllegalArgumentException if the [.needsHeadSet] method returns
     * true.
     * @since JWI 2.2.0
     */
    private fun checkHeadSet() {
        check(!needsHeadSet()) { "Head word and id not yet set" }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + lemma.hashCode()
        result = prime * result + this.lexicalID
        checkNotNull(this.pOS)
        result = prime * result + pOS.hashCode()
        checkNotNull(this.lexicalFile)
        result = prime * result + lexicalFile.hashCode()
        result = prime * result + (if (this.isAdjectiveSatellite) 1231 else 1237)
        if (this.isAdjectiveSatellite) {
            result = prime * result + (headLemma?.hashCode() ?: 0)
            result = prime * result + headLexID
        }
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
        if (obj !is SenseKey) {
            return false
        }
        val other = obj
        if (lemma != other.lemma) {
            return false
        }
        if (this.lexicalID != other.lexicalID) {
            return false
        }
        if (this.pOS != other.pOS) {
            return false
        }
        checkNotNull(other.lexicalFile)
        checkNotNull(this.lexicalFile)
        if (lexicalFile.number != other.lexicalFile.number) {
            return false
        }
        if (this.isAdjectiveSatellite != other.isAdjectiveSatellite) {
            return false
        }
        if (this.isAdjectiveSatellite) {
            checkNotNull(headLemma)
            if (headLemma != other.headWord) {
                return false
            }
            return headLexID == other.headID
        }
        return true
    }

    companion object {

        /**
         * Returns a string representation of the specified sense key object.
         *
         * @param key the sense key to be encoded as a string
         * @return the string representation of the sense key
         * @throws NullPointerException if the specified key is null
         * @since JWI 2.1.0
         */

        fun toString(key: ISenseKey): String {
            val lf: ILexFile? = checkNotNull(key.lexicalFile)
            // figure out appropriate size
            var size = key.lemma.length + 10
            if (key.isAdjectiveSatellite) {
                val hw: String? = checkNotNull(key.headWord)
                size += hw!!.length + 2
            }

            // allocate builder
            val sb = StringBuilder(size)

            // make string
            sb.append(key.lemma) //.toLowerCase());
            sb.append('%')
            sb.append(key.synsetType)
            sb.append(':')
            sb.append(LexFile.getLexicalFileNumberString(lf!!.number))
            sb.append(':')
            sb.append(getLexicalIDForSenseKey(key.lexicalID))
            sb.append(':')
            if (key.isAdjectiveSatellite) {
                if (key.needsHeadSet()) {
                    sb.append("??")
                } else {
                    sb.append(key.headWord)
                }
            }
            sb.append(':')
            if (key.isAdjectiveSatellite) {
                if (key.needsHeadSet()) {
                    sb.append("??")
                } else {
                    sb.append(getLexicalIDForSenseKey(key.headID))
                }
            }
            return sb.toString()
        }
    }
}
