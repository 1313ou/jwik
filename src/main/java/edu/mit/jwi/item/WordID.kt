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
import edu.mit.jwi.item.Word.Companion.checkWordNumber
import edu.mit.jwi.item.Word.Companion.zeroFillWordNumber
import java.util.*

/**
 * Default implementation of the `IWordID` interface.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
class WordID : IWordID {

    override val synsetID: ISynsetID

    override val wordNumber: Int

    override val pOS: POS
        get() = synsetID.pOS!!

    override val lemma: String?

    /**
     * Constructs a word id from the specified arguments. This constructor
     * produces a word with an unknown lemma.
     *
     * @param offset the synset offset
     * @param pos    the part of speech; may not be `null`
     * @param num    the word number
     * @throws IllegalArgumentException if the offset or number are not legal
     * @since JWI 1.0
     */
    constructor(offset: Int, pos: POS, num: Int) : this(SynsetID(offset, pos), num)

    /**
     * Constructs a word id from the specified arguments. This constructor
     * produces a word with an unknown word number.
     *
     * @param offset the synset offset
     * @param pos    the part of speech; may not be `null`
     * @param lemma  the lemma; may not be `null`, empty, or all
     * whitespace
     * @since JWI 1.0
     */
    constructor(offset: Int, pos: POS, @NonNull lemma: String) : this(SynsetID(offset, pos), lemma)

    /**
     * Constructs a word id from the specified arguments. This constructor
     * produces a word with an unknown lemma.
     *
     * @param id  the synset id; may not be `null`
     * @param num the word number
     * @throws NullPointerException     if the synset id is `null`
     * @throws IllegalArgumentException if the lemma is empty or all whitespace
     * @since JWI 1.0
     */
    constructor(@Nullable id: ISynsetID, num: Int) {
        if (id == null) {
            throw NullPointerException()
        }
        checkWordNumber(num)
        this.synsetID = id
        this.wordNumber = num
        this.lemma = null
    }

    /**
     * Constructs a word id from the specified arguments. This constructor
     * produces a word with an unknown word number.
     *
     * @param id    the synset id; may not be `null`
     * @param lemma the lemma; may not be `null`, empty, or all
     * whitespace
     * @throws NullPointerException     if the synset id is `null`
     * @throws IllegalArgumentException if the lemma is empty or all whitespace
     * @since JWI 1.0
     */
    constructor(@Nullable id: ISynsetID, @NonNull lemma: String) {
        if (id == null) {
            throw NullPointerException()
        }
        require(lemma.trim { it <= ' ' }.isNotEmpty())
        this.synsetID = id
        this.wordNumber = -1
        this.lemma = lemma.lowercase(Locale.getDefault())
    }

    /**
     * Constructs a fully specified word id
     *
     * @param id    the synset id; may not be `null`
     * @param num   the word number
     * @param lemma the lemma; may not be `null`, empty, or all
     * whitespace
     * @throws NullPointerException     if the synset id is `null`
     * @throws IllegalArgumentException if the lemma is empty or all whitespace, or the word number
     * is not legal
     * @since JWI 1.0
     */
    constructor(@Nullable id: ISynsetID, num: Int, @NonNull lemma: String) {
        if (id == null) {
            throw NullPointerException()
        }
        require(lemma.trim { it <= ' ' }.length != 0)
        checkWordNumber(num)
        this.synsetID = id
        this.wordNumber = num
        this.lemma = lemma
    }

    /*
    * (non-Javadoc)
    *
    * @see java.lang.Object#hashCode()
    */
    override fun hashCode(): Int {
        checkNotNull(this.synsetID)
        return 31 * synsetID.hashCode()
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    override fun equals(@Nullable obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as WordID
        checkNotNull(this.synsetID)
        if (this.synsetID != other.synsetID) {
            return false
        }
        if (other.wordNumber != -1 && this.wordNumber != -1 && other.wordNumber != this.wordNumber) {
            return false
        }
        if (other.lemma != null && lemma != null) {
            return other.lemma.equals(lemma, ignoreCase = true)
        }
        return true
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @NonNull
    override fun toString(): String {
        checkNotNull(this.synsetID)
        val pos = checkNotNull(synsetID.pOS)
        return wordIDPrefix +  //
                Synset.zeroFillOffset(synsetID.offset) +  //
                '-' + pos.tag.uppercaseChar() +  //
                '-' +  //
                (if (this.wordNumber < 0) unknownWordNumber else zeroFillWordNumber(this.wordNumber)) +  //
                '-' +  //
                (if (lemma == null) unknownLemma else lemma)
    }

    companion object {

        /**
         * Generated serial version id.
         *
         * @since JWI 2.2.5
         */
        private const val serialVersionUID = 3163309710173885763L

        /**
         * String prefix for the [.toString] method.
         *
         * @since JWI 2.0.0
         */
        const val wordIDPrefix: String = "WID-"

        /**
         * Represents an unknown lemma for the [.toString] method.
         *
         * @since JWI 2.0.0
         */
        const val unknownLemma: String = "?"

        /**
         * Represents an unknown word number for the [.toString] method.
         *
         * @since JWI 2.0.0
         */
        const val unknownWordNumber: String = "??"

        /**
         * Parses the result of the [.toString] method back into an
         * `WordID`. Word ids are always of the following format:
         * WID-########-P-##-lemma where ######## is the eight decimal digit
         * zero-filled offset of the associated synset, P is the upper case
         * character representing the part of speech, ## is the two hexadecimal
         * digit zero-filled word number (or ?? if unknown), and lemma is the lemma.
         *
         * @param value the string to be parsed
         * @return WordID the parsed id
         * @throws IllegalArgumentException if the specified string does not represent a word id
         * @throws NullPointerException     if the specified string is `null`
         * @since JWI 1.0
         */
        @NonNull
        fun parseWordID(@Nullable value: String): IWordID {
            if (value == null) {
                throw NullPointerException()
            }
            require(value.length >= 19)
            require(value.startsWith("WID-"))

            // get synset id
            val offset = value.substring(4, 12).toInt()
            val pos = POS.getPartOfSpeech(value.get(13))
            val id: ISynsetID = SynsetID(offset, pos)

            // get word number
            val numStr = value.substring(15, 17)
            if (numStr != unknownWordNumber) {
                val num = numStr.toInt(16)
                return WordID(id, num)
            }

            // get lemma
            val lemma = value.substring(18)
            require(lemma != unknownLemma)
            return WordID(id, lemma)
        }
    }
}
