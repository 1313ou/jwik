package edu.mit.jwi.item

import edu.mit.jwi.item.Synset.Companion.checkLexicalID
import java.io.Serializable
import java.util.*

/**
 * Sense Key
 *
 * @param lemma unprocessed lemma
 * @property lemma processed lemma
 * @property lexID lexical id for this sense key
 * @property pOS part-of-speech
 * @property headWord head word for adjective satellite
 * @property headID head word ID for adjective satellite
 * @property lexicalFileNum lexical File
 */
class SenseKey(
    /**
     * Lemma
     */
    val casedLemma: String,

    /**
     * Part-of-Speech
     */
    override val pOS: POS,

    /**
     * Lexical File number
     */
    val lexicalFileNum: Int,

    /**
     * The lexical id for this sense key, which is a non-negative integer.
     * lex_id is a two digit decimal integer that, when appended onto lemma , uniquely identifies a sense within a lexicographer file.
     * lex_id numbers usually start with 00 , and are incremented as additional senses of the word are added to the same file, although there is no requirement that the numbers be consecutive or begin with 00 .
     * Note that a value of 00 is the default
     */
    val lexID: Int,

    /**
     * The head word for adjective satellite
     * The head id is only non-null if the sense is an adjective satellite synset,
     */
    val headWord: String? = null,

    /**
     * The head id for adj satellite
     * The head id is only present if the sense is an adjective satellite synset,
     * It is a two digit decimal integer that, when appended onto the head word, uniquely identifies the sense within a lexicographer file.
     * If this sense key is not for an adjective synset, this method returns `-1`.
     */
    val headID: Int? = null,

    ) : IHasPOS, Comparable<SenseKey>, Serializable {

    /**
     * Lemma
     */
    val lemma: String = casedLemma.asSensekeyLemma()

    /**
     * Whether the sense is an adjective satellite
     */
    val isAdjectiveSatellite: Boolean
        get() = headWord != null || headID != null

    /**
     * (Cached) string
     */
    val sensekey: String by lazy { toString() }

    /**
     * (Cached) string
     */
    val key: String by lazy { toKeyString() }

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
            return if (isAdjectiveSatellite) NUM_ADJECTIVE_SATELLITE else pOS.number
        }

    init {
        if (isAdjectiveSatellite) {
            checkLexicalID(headID!!)
            require(headWord!!.isNotEmpty())
        }
    }

    override fun compareTo(other: SenseKey): Int {

        // first sort alphabetically by lemma
        var cmp = lemma.compareTo(other.lemma) // ignoreCase = true not needed if lemma is lowercased in constructor
        if (cmp != 0) {
            return cmp
        }

        // then sort by synset type
        cmp = synsetType.compareTo(other.synsetType)
        if (cmp != 0) {
            return cmp
        }

        // then sort by lex_filenum
        cmp = lexicalFileNum.compareTo(other.lexicalFileNum)
        if (cmp != 0) {
            return cmp
        }

        // then sort by lex_id
        cmp = lexID.compareTo(other.lexID)
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
        return headID!!.compareTo(other.headID!!)
    }

    override fun toString(): String {
        return if (isAdjectiveSatellite)
            "$lemma%$NUM_ADJECTIVE_SATELLITE:${"%02d".format(lexicalFileNum)}:${"%02d".format(lexID)}:$headWord:${"%02d".format(headID)}"
        else
            "$lemma%${pOS.number}:${"%02d".format(lexicalFileNum)}:${"%02d".format(lexID)}::"
    }

    fun toKeyString(): String {
        return if (isAdjectiveSatellite)
            "$casedLemma%$NUM_ADJECTIVE_SATELLITE:${"%02d".format(lexicalFileNum)}:${"%02d".format(lexID)}:$headWord:${"%02d".format(headID)}"
        else
            "$casedLemma%${pOS.number}:${"%02d".format(lexicalFileNum)}:${"%02d".format(lexID)}::"
    }

    override fun hashCode(): Int {
        return Objects.hash(lemma, lexID, pOS, lexicalFileNum, isAdjectiveSatellite, headWord, headID)
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
        if (lexID != other.lexID) {
            return false
        }
        if (pOS != other.pOS) {
            return false
        }
        if (lexicalFileNum != other.lexicalFileNum) {
            return false
        }
        if (isAdjectiveSatellite != other.isAdjectiveSatellite) {
            return false
        }
        if (isAdjectiveSatellite) {
            if (headWord != other.headWord) {
                return false
            }
            return headID == other.headID
        }
        return true
    }
}
