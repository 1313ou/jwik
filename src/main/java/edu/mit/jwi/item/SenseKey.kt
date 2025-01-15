package edu.mit.jwi.item

import edu.mit.jwi.item.LexFile.Companion.getLexicalFileNumberString
import edu.mit.jwi.item.Word.Companion.checkLexicalID
import edu.mit.jwi.item.Word.Companion.getLexicalIDForSenseKey
import java.io.Serializable
import java.util.*

/**
 * Sense Key
 *
 * @param lemma unprocessed lemma
 * @property lemma processed lemma
 * @property lexicalID lexical id for this sense key
 * @property pOS part of speech
 * @property isAdjectiveSatellite whether this sense key points to an adjective satellite
 * @property lexicalFile lexical File
 */
class SenseKey(
    /**
     * Lemma
     */
    lemma: String,

    /**
     * The lexical id for this sense key, which is a non-negative integer.
     */
    val lexicalID: Int,

    /**
     * Part of Speech
     */
    override val pOS: POS,

    /**
     * Whether this sense key points to an adjective satellite
     */
    val isAdjectiveSatellite: Boolean,

    /**
     * Lexical File
     */
    val lexicalFile: LexFile,

    ) : IHasPOS, Comparable<SenseKey>, Serializable {

    /**
     * Lemma
     */
    val lemma: String = lemma.lowercase()

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

    private var sensekey: String? = null
        get() {
            checkHeadSet()
            if (field == null) {
                field = toString(this)
            }
            return field
        }

    /**
     * Constructs a new sense key.
     *
     * @param lemma  the lemma for the sense key
     * @param lexicalID the lexical id of the sense key
     * @param synset the synset for the sense key
     */
    constructor(lemma: String, lexicalID: Int, synset: Synset) : this(lemma, lexicalID, synset.pOS, synset.isAdjectiveSatellite, synset.lexicalFile)

    /**
     * Constructs a new sense key.
     *
     * @param lemma       the lemma
     * @param lexID       the lexical id
     * @param pos         the part of speech
     * @param isAdjSat    true if this represents an adjective satellite; false otherwise
     * @param lexFile     the lexical file
     * @param sensekey    the original key string
     */
    constructor(lemma: String, lexID: Int, pos: POS, isAdjSat: Boolean, lexFile: LexFile, sensekey: String) : this(lemma, lexID, pos, isAdjSat, lexFile) {
        this.sensekey = sensekey
    }

    /**
     * Constructs a new sense key.
     *
     * @param lemma       the lemma
     * @param lexID       the lexical id
     * @param pos         the part of speech
     * @param lexFile     the lexical file
     * @param sensekey    the original key string
     * @param headLemma   the head lemma
     * @param headLexID   the head lexical id; ignored if head lemma is null
     */
    constructor(lemma: String, lexID: Int, pos: POS, lexFile: LexFile, headLemma: String?, headLexID: Int, sensekey: String) : this(lemma, lexID, pos, (headLemma != null), lexFile) {
        if (headLemma == null) {
            isHeadSet = true
        } else {
            setHead(headLemma, headLexID)
        }
        this.sensekey = sensekey
    }

    /**
     * This method is used to set the head for sense keys for adjective satellites, and it can only be called once, directly after the relevant word is created.
     * If this method is called on a sense key that has had its head set already, or is not an adjective satellite, it will throw an exception.
     *
     * @param headLemma the head lemma to be set
     * @param headLexID the head lexid to be set
     * @throws IllegalStateException if this method has already been called, if the headLemma is empty or all whitespace or if the headLexID is illegal.
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
     * Whether the head lemma and lexical id need to be set
     * This method will always return false if the isAdjectiveSatellite returns false.
     * If that method returns true, this method will only return true if setHead has not yet been called.
     */
    fun needsHeadSet(): Boolean {
        return !isHeadSet
    }

    /**
     * Throws an exception if the head is not yet set.
     *
     * @throws IllegalArgumentException if the needsHeadSet method returns true.
     */
    private fun checkHeadSet() {
        check(!needsHeadSet()) { "Head word and id not yet set" }
    }

    override fun compareTo(other: SenseKey): Int {

        // first sort alphabetically by lemma
        var cmp: Int = lemma.compareTo(other.lemma) //, ignoreCase = true) not needed if lemma is lowercase
        if (cmp != 0) {
            return cmp
        }

        // then sort by synset type
        cmp = synsetType.toInt().compareTo(other.synsetType.toInt())
        if (cmp != 0) {
            return cmp
        }

        // then sort by lex_filenum
        val lf = lexicalFile
        val lf2: LexFile? = other.lexicalFile
        cmp = lf.number.toInt().compareTo(lf2!!.number.toInt())
        if (cmp != 0) {
            return cmp
        }

        // then sort by lex_id
        cmp = lexicalID.toInt().compareTo(other.lexicalID.toInt())
        if (cmp != 0) {
            return cmp
        }

        // then by adjective satellite property
        when {
            !isAdjectiveSatellite && !other.isAdjectiveSatellite -> return 0
            !isAdjectiveSatellite and other.isAdjectiveSatellite -> return -1
            isAdjectiveSatellite and !other.isAdjectiveSatellite -> return 1
        }

        // then sort by head_word
        cmp = headWord!!.compareTo(other.headWord!!)
        if (cmp != 0) {
            return cmp
        }

        // finally by head_id
        return headID.toInt().compareTo(other.headID.toInt())
    }

    override fun toString(): String {
        return sensekey!!
    }

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
         */
        fun toString(key: SenseKey): String {
            val lexFileNum = getLexicalFileNumberString(key.lexicalFile.number)
            val lexID = getLexicalIDForSenseKey(key.lexicalID)
            val head = if (key.isAdjectiveSatellite) (if (key.needsHeadSet()) "??" else key.headWord) else ""
            val headID = if (key.isAdjectiveSatellite) (if (key.needsHeadSet()) "??" else getLexicalIDForSenseKey(key.headID)) else ""
            return "${key.lemma}%${key.synsetType}:$lexFileNum:$lexID$:$head:$headID"
        }
    }
}
