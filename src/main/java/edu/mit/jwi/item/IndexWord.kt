package edu.mit.jwi.item

import java.util.*

/**
 * A Wordnet index word object, represented in the Wordnet files as a line in an index file.
 *
 * Constructs a new index word.
 *
 * @param id          the index word id for this index word
 * @param tagSenseCnt the tag sense count
 * @param ptrs        an array of pointers for all the synsets of this lemma
 * @param words       the words for this index word
 * @throws IllegalArgumentException if the tag sense count is negative, or the word array is empty
 */
class IndexWord(
    id: IndexWordID,
    tagSenseCnt: Int,
    ptrs: Array<Pointer>?,
    words: Array<IWordID>,

    ) : IHasPOS, IItem<IndexWordID> {

    /**
     * The lemma (word root) associated with this index word.
     * Never empty or all whitespace.
     */
    override val iD: IndexWordID = id

    /**
     * The number of senses of lemma that are ranked according to their frequency of occurrence in semantic concordance texts.
     * This will be a non-negative number.
     */
    val tagSenseCount: Int = tagSenseCnt

    /**
     * An immutable set containing all the different types of pointers that this index word has across all synsets containing this word.
     * If all senses of the word have no pointers, this method returns an empty set.
     */
    val pointers: Set<Pointer>

    val wordIDs: List<IWordID> = words.toList()

    val lemma: String
        get() {
            return iD.lemma
        }

    override val pOS: POS
        get() {
            return iD.pOS
        }

    /**
     * Constructs a new index word.
     *
     * @param lemma       the lemma of this index word
     * @param pos         the part of speech of this index word
     * @param tagSenseCnt the tag sense count
     * @param words       the words for this index word
     * @throws IllegalArgumentException if the tag sense count is negative, or the word array is empty
     */
    constructor(lemma: String, pos: POS, tagSenseCnt: Int, words: Array<IWordID>) : this(IndexWordID(lemma, pos), tagSenseCnt, null, words)

    /**
     * Constructs a new index word.
     *
     * @param lemma       the lemma of this index word
     * @param pos         the part of speech of this index word
     * @param tagSenseCnt the tag sense count
     * @param ptrs        an array of pointers that the synsets with lemma have
     * @param words       the words for this index word
     * @throws IllegalArgumentException if the tag sense count is negative, or the word array is empty
     */
    constructor(lemma: String, pos: POS, tagSenseCnt: Int, ptrs: Array<Pointer>, words: Array<IWordID>) : this(IndexWordID(lemma, pos), tagSenseCnt, ptrs, words)

    /**
     * Constructs a new index word.
     *
     * @param id          the index word id for this index word
     * @param tagSenseCnt the tag sense count
     * @param words       the words for this index word
     * @throws IllegalArgumentException if the tag sense count is negative, or the word array is empty
     */
    constructor(id: IndexWordID, tagSenseCnt: Int, words: Array<IWordID>) : this(id, tagSenseCnt, null, words)

    init {
        require(tagSenseCnt >= 0)
        require(words.isNotEmpty())

        // do pointers as of v2.3.0
        val pointers: MutableSet<Pointer>
        if (ptrs == null || ptrs.isEmpty()) {
            pointers = mutableSetOf<Pointer>()
        } else {
            pointers = HashSet<Pointer>(ptrs.size)
            for (p in ptrs) {
                pointers.add(p)
            }
        }
        this.pointers = pointers
    }

    override fun toString(): String {
        return "[$iD${iD.lemma} (${iD.pOS}) ${wordIDs.joinToString(separator = ", ")}]"
    }

    override fun hashCode(): Int {
        return Objects.hash(iD, tagSenseCount, wordIDs, pointers)
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (obj !is IndexWord) {
            return false
        }
        val other: IndexWord = obj
        if (iD != other.iD) {
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
