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
 * Default implementation of `IIndexWord`.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
class IndexWord(id: IIndexWordID, tagSenseCnt: Int, ptrs: Array<IPointer>?, words: Array<IWordID>) : IIndexWord {

    override val iD: IIndexWordID

    override val tagSenseCount: Int

    override val pointers: Set<IPointer>

    override val wordIDs: List<IWordID>

     override val lemma: String
        get() {
            checkNotNull(this.iD)
            return iD.lemma
        }

    override val pOS: POS
        get() {
            checkNotNull(this.iD)
            return iD.pOS
        }

   /**
     * Constructs a new index word.
     *
     * @param lemma       the lemma of this index word
     * @param pos         the part of speech of this index word
     * @param tagSenseCnt the tag sense count
     * @param words       the words for this index word
     * @throws NullPointerException     if lemma, pos, or word array is `null`, or the
     * word array contains null
     * @throws IllegalArgumentException if the tag sense count is negative, or the word array is
     * empty
     * @since JWI 1.0
     */
    constructor(lemma: String, pos: POS, tagSenseCnt: Int, words: Array<IWordID>) : this(IndexWordID(lemma, pos), tagSenseCnt, null, words)

    /**
     * Constructs a new index word.
     *
     * @param lemma       the lemma of this index word
     * @param pos         the part of speech of this index word
     * @param tagSenseCnt the tag sense count
     * @param ptrs        an array of pointers that the synsets with lemma have; may be
     * `null`
     * @param words       the words for this index word
     * @throws NullPointerException     if lemma, pos, or word array is `null`, or the
     * word array or pointer array contains `null`
     * @throws IllegalArgumentException if the tag sense count is negative, or the word array is
     * empty
     * @since JWI 2.3.0
     */
    constructor(lemma: String, pos: POS, tagSenseCnt: Int, ptrs: Array<IPointer>, words: Array<IWordID>) : this(IndexWordID(lemma, pos), tagSenseCnt, ptrs, words)

    /**
     * Constructs a new index word.
     *
     * @param id          the index word id for this index word
     * @param tagSenseCnt the tag sense count
     * @param words       the words for this index word
     * @throws NullPointerException     if lemma, pos, or word array is `null`, or the
     * word array contains null
     * @throws IllegalArgumentException if the tag sense count is negative, or the word array is
     * empty
     * @since JWI 1.0
     */
    constructor(id: IIndexWordID, tagSenseCnt: Int, words: Array<IWordID>) : this(id, tagSenseCnt, null, words)

    /**
     * Constructs a new index word.
     *
     * @param id          the index word id for this index word
     * @param tagSenseCnt the tag sense count
     * @param ptrs        an array of pointers for all the synsets of this lemma; may be
     * `null`; must not contain `null`
     * @param words       the words for this index word
     * @throws NullPointerException     if lemma, pos, or word array is `null`, or the
     * word array or pointer array contains `null`
     * @throws IllegalArgumentException if the tag sense count is negative, or the word array is
     * empty
     * @since JWI 2.3.0
     */
    init {
        if (id == null) {
            throw NullPointerException()
        }
        require(tagSenseCnt >= 0)
        require(words.isNotEmpty())
        for (wid in words) {
            if (wid == null) {
                throw NullPointerException()
            }
        }

        // do pointers as of v2.3.0
        val pointers: MutableSet<IPointer>
        if (ptrs == null || ptrs.isEmpty()) {
            pointers = mutableSetOf<IPointer>()
        } else {
            pointers = HashSet<IPointer>(ptrs.size)
            for (p in ptrs) {
                if (p == null) {
                    throw NullPointerException()
                } else {
                    pointers.add(p)
                }
            }
        }

        this.iD = id
        this.tagSenseCount = tagSenseCnt
        this.wordIDs = Collections.unmodifiableList<IWordID>(listOf<IWordID>(*words))
        this.pointers = pointers
    }

    /*
    * (non-Javadoc)
    *
    * @see java.lang.Object#toString()
    */
    @NonNull
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('[')
        checkNotNull(this.iD)
        sb.append(iD.lemma)
        sb.append(" (")
        sb.append(iD.pOS)
        sb.append(") ")
        val i: Iterator<IWordID> = wordIDs.iterator()
        while (i.hasNext()) {
            sb.append(i.next().toString())
            if (i.hasNext()) {
                sb.append(", ")
            }
        }
        sb.append(']')
        return sb.toString()
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        checkNotNull(this.iD)
        result = prime * result + iD.hashCode()
        result = prime * result + tagSenseCount
        result = prime * result + wordIDs.hashCode()
        result = prime * result + pointers.hashCode()
        return result
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
        if (obj !is IIndexWord) {
            return false
        }
        val other: IIndexWord = obj as IndexWord
        checkNotNull(this.iD)
        if (this.iD != other.iD) {
            return false
        }
        if (tagSenseCount != other.tagSenseCount) {
            return false
        }
        if (wordIDs != other.wordIDs) {
            return false
        }
        return pointers == other.pointers
    }
}
