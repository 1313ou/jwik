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
import java.io.Serializable
import java.util.*

/**
 * Concrete, default implementation of the `SenseKey` interface.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.1.0
 */
class SenseKey(

    val lemma: String,

    /**
     * The lexical id for this sense key, which is a non-negative integer.
     */
    val lexicalID: Int,

    override val pOS: POS,

    /**
     * Whether this sense key points to an adjective satellite
     */
    val isAdjectiveSatellite: Boolean,

    val lexicalFile: LexFile,
) : IHasPOS, Comparable<SenseKey>, Serializable {

    /**
     * The synset type for the key.
     * The synset type is a one digit decimal integer representing the synset type for the sense.
     * 1=NOUN
     * 2=VERB
     * 3=ADJECTIVE
     * 4=ADVERB
     * 5=ADJECTIVE SATELLITE
     */
    val synsetType: Int
        get() {
            return if (this.isAdjectiveSatellite) NUM_ADJECTIVE_SATELLITE else pOS.number
        }

    val headWord: String?
        get() {
            checkHeadSet()
            return headLemma
        }

    /**
     * The head id for this sense key
     * The head id is only present if the sense is an adjective satellite synset,
     * It is a two digit decimal integer that, when appended onto the head word, uniquely identifies the sense within a lexicographer file.
     * If this sense key is not for an adjective synset, this method returns `-1`.
     */
    val headID: Int
        get() {
            checkHeadSet()
            return headLexID
        }

    private var isHeadSet: Boolean = !isAdjectiveSatellite

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
    constructor(lemma: String, lexID: Int, synset: Synset) : this(lemma, lexID, synset.pOS, synset.isAdjectiveSatellite, synset.lexicalFile)

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
    constructor(lemma: String, lexID: Int, pos: POS, isAdjSat: Boolean, lexFile: LexFile, originalKey: String) : this(lemma, lexID, pos, isAdjSat, lexFile) {
        toString = originalKey
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
    constructor(lemma: String, lexID: Int, pos: POS, lexFile: LexFile, headLemma: String?, headLexID: Int, originalKey: String) : this(lemma, lexID, pos, (headLemma != null), lexFile) {
        if (headLemma == null) {
            isHeadSet = true
        } else {
            setHead(headLemma, headLexID)
        }
        this.toString = originalKey
    }

    /**
     * This method is used to set the head for sense keys for adjective
     * satellites, and it can only be called once, directly after the relevant
     * word is created. If this method is called on a sense key that has had its
     * head set already, or is not an adjective satellite, it will throw an
     * exception.
     *
     * @param headLemma the head lemma to be set
     * @param headLexID the head lexid to be set
     * @throws IllegalStateException if this method has already been called, if the headLemma is empty or all whitespace or if the headLexID is illegal.
     * @since JWI 2.1.0
     */
    fun setHead(headLemma: String, headLexID: Int) {
        check(needsHeadSet())
        checkLexicalID(headLexID)
        require(headLemma.trim { it <= ' ' }.isNotEmpty())
        this.headLemma = headLemma
        this.headLexID = headLexID
        this.isHeadSet = true
    }

    /**
     * Whether the head lemma and lexical id need to be  set
     * This method will always return false if the [.isAdjectiveSatellite] returns false.
     * If that method returns true, this method will only return true if [.setHead] has not yet been called.
     *
     */
    fun needsHeadSet(): Boolean {
        return !isHeadSet
    }

    override fun compareTo(key: SenseKey): Int {

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
        val lf2: LexFile? = checkNotNull(key.lexicalFile)
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
        return Objects.hash(lemma, lexicalID, pOS, lexicalFile, isAdjectiveSatellite, headLemma, headLexID)
    }

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
        if (lexicalID != other.lexicalID) {
            return false
        }
        if (pOS != other.pOS) {
            return false
        }
        if (lexicalFile.number != other.lexicalFile.number) {
            return false
        }
        if (isAdjectiveSatellite != other.isAdjectiveSatellite) {
            return false
        }
        if (isAdjectiveSatellite) {
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

        fun toString(key: SenseKey): String {
            val lf: LexFile? = checkNotNull(key.lexicalFile)
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
