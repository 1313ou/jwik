package edu.mit.jwi.item

import java.util.*

/**
 * A Wordnet index object, represented in the Wordnet files as a line in an index file index.(noun|verb|adj|adv)
 *
 * Each index file is an alphabetized list of all the words found in WordNet in the corresponding part-of-speech.
 * On each line, following the word, is a list of byte offsets (synset_offset s) in the corresponding data file, one for each synset containing the word.
 * Words in the index file are in lower case only, regardless of how they were entered in the lexicographer files.
 * This folds various orthographic representations of the word into one line enabling database searches to be case-insensitive.
 *
 * Constructs a new index.
 *
 * @param id the index id for this index
 * @param tagSenseCnt the tag sense count
 * @param pointers an array of pointers for all the synsets of this lemma
 * @param senseids the sense ids for this index
 * @throws IllegalArgumentException if the tag sense count is negative, or the sense ids array is empty
 */
class Index(
    id: IndexID,
    tagSenseCnt: Int,
    pointers: Array<Pointer>?,
    senseids: Array<SenseID>,

    ) : IHasPOS, IItem<IndexID> {

    /**
     * The index ID / key
     */
    override val iD: IndexID = id

    /**
     * The lemma associated with this index.
     * Derived from id.
     * Never empty or all whitespace.
     */
    val lemma: String
        get() {
            return iD.lemma
        }

    /**
     * Part-of-speech
     * Derived from id.
     */
    override val pOS: POS
        get() {
            return iD.pOS
        }

    /**
     * An immutable set containing all the different types of pointers that this index has across all synsets referring to this sense.
     * If all senses of the word have no pointers, this method returns an empty set.
     */
    val pointers: Set<Pointer> = pointers?.toSet() ?: emptySet()

   /**
     * The number of senses of lemma that are ranked according to their frequency of occurrence in semantic concordance texts.
     * This will be a non-negative number.
     */
    val tagSenseCount: Int = tagSenseCnt

    /**
     * Sense IDs
     */
    val senseIDs: List<SenseID> = senseids.toList()

    /**
     * Constructs a new index.
     *
     * @param lemma the lemma of this index
     * @param pos the part-of-speech of this index
     * @param tagSenseCnt the tag sense count
     * @param senseids the sense ids for this index
     * @throws IllegalArgumentException if the tag sense count is negative, or the sense ids array is empty
     */
    constructor(lemma: String, pos: POS, tagSenseCnt: Int, senseids: Array<SenseID>) : this(IndexID(lemma, pos), tagSenseCnt, null, senseids)

    /**
     * Constructs a new index.
     *
     * @param lemma the lemma of this index
     * @param pos the part-of-speech of this index
     * @param tagSenseCnt the tag sense count
     * @param pointers an array of pointers that the synsets with lemma have
     * @param senseids the sense ids for this index
     * @throws IllegalArgumentException if the tag sense count is negative, or the sense ids array is empty
     */
    constructor(lemma: String, pos: POS, tagSenseCnt: Int, pointers: Array<Pointer>, senseids: Array<SenseID>) : this(IndexID(lemma, pos), tagSenseCnt, pointers, senseids)

    /**
     * Constructs a new index.
     *
     * @param id the index id for this index
     * @param tagSenseCnt the tag sense count
     * @param senseids the sense ids for this index
     * @throws IllegalArgumentException if the tag sense count is negative, or the sense ids array is empty
     */
    constructor(id: IndexID, tagSenseCnt: Int, senseids: Array<SenseID>) : this(id, tagSenseCnt, null, senseids)

    init {
        require(tagSenseCnt >= 0)
        require(senseids.isNotEmpty())
    }

    override fun toString(): String {
        return "[$iD${iD.lemma} (${iD.pOS}) ${senseIDs.joinToString(separator = ", ")}]"
    }

    override fun hashCode(): Int {
        return Objects.hash(iD, tagSenseCount, senseIDs, pointers)
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (obj !is Index) {
            return false
        }
        val other: Index = obj
        if (iD != other.iD) {
            return false
        }
        if (tagSenseCount != other.tagSenseCount) {
            return false
        }
        if (senseIDs != other.senseIDs) {
            return false
        }
        return pointers == other.pointers
    }
}
