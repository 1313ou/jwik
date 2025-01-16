package edu.mit.jwi.item

import edu.mit.jwi.item.LexFile.Companion.ADJ_ALL
import edu.mit.jwi.item.Sense.Companion.checkWordNumber
import java.util.*

/**
 * Synset
 *
 * @property iD the synset id
 * @property lexicalFile the lexical file for this synset
 * @property isAdjectiveSatellite true if this object represents an adjective satellite synset; false otherwise
 * @property isAdjectiveHead true if this object represents an adjective head synset; false otherwise
 * @property gloss the gloss for this synset
 * @property words the list of words in this synset
 * @property related a map of related synset lists, indexed by pointer
 * @throws IllegalArgumentException if the word list is empty, or both the adjective satellite and adjective head flags are set
 * @throws IllegalArgumentException if either the adjective satellite and adjective head flags are set, and the lexical file number is not zero
 */
class Synset private constructor(
    /**
     * Synset ID
     */
    override val iD: SynsetID,

    /**
     * The lexical file it was found in
     */
    val lexicalFile: LexFile,

    /**
     * Whether this synset is / represents an adjective satellite
     */
    val isAdjectiveSatellite: Boolean,

    /**
     * Whether this synset is / represents an adjective head
     */
    val isAdjectiveHead: Boolean,

    /**
     * The gloss or definition that comes with the synset
     */
    val gloss: String,

    /**
     * Semantic relations
     */
    val related: Map<Pointer, List<SynsetID>>,

    ) : IHasPOS, IItem<SynsetID> {

    /**
     * Part Of Speech
     */
    override val pOS: POS
        get() {
            return iD.pOS
        }

    /**
     * The data file byte offset of this synset in the associated data source
     */
    val offset: Int
        get() {
            return iD.offset
        }

    /**
     * The type of the synset, encoded as follows:
     * 1=Noun,
     * 2=Verb,
     * 3=Adjective,
     * 4=Adverb,
     * 5=Adjective Satellite.
     */
    val type: Int
        get() {
            val pos = pOS
            if (pos == POS.ADJECTIVE) {
                return if (isAdjectiveSatellite) NUM_ADJECTIVE_SATELLITE else NUM_ADJECTIVE
            }
            return pos.number
        }

    /**
     * The words that are members of the synset
     */
    lateinit var words: List<Sense>

    /**
     * Default implementation of the `Synset` interface.
     *
     * @param iD the synset id
     * @param lexicalFile the lexical file for this synset
     * @param isAdjectiveSatellite true if this object represents an adjective satellite synset; false otherwise
     * @param isAdjectiveHead true if this object represents an adjective head synset; false otherwise
     * @param gloss the gloss for this synset
     * @param wordBuilders the list of word builders for this synset
     * @param related a map of related synset lists, indexed by pointer
     */
    constructor(
        iD: SynsetID,
        lexicalFile: LexFile,
        isAdjectiveSatellite: Boolean,
        isAdjectiveHead: Boolean,
        gloss: String,
        wordBuilders: List<IWordBuilder>,
        related: Map<Pointer, List<SynsetID>>?,
    ) : this(iD, lexicalFile, isAdjectiveSatellite, isAdjectiveHead, gloss, normalizeRelated(related)) {
        require(!wordBuilders.isEmpty())
        words = buildWords(wordBuilders, this)
    }

    init {
        require(!(isAdjectiveSatellite && isAdjectiveHead))
        require(!((isAdjectiveSatellite || isAdjectiveHead) && lexicalFile.number != ADJ_ALL.number))
    }

    override fun hashCode(): Int {
        return Objects.hash(iD, words, related, gloss, isAdjectiveSatellite)
    }

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
        if (iD != other.iD) {
            return false
        }
        if (words != other.words) {
            return false
        }
        if (gloss != other.gloss) {
            return false
        }
        if (isAdjectiveSatellite != other.isAdjectiveSatellite) {
            return false
        }
        return related == other.related
    }

    override fun toString(): String {
        return "S-{${iD} [${words.joinToString(separator = ", ")}]}"
    }

    /**
     * List of the ids of all synsets that are related to this synset by the specified pointer type.
     * Note that this only returns a non-empty result for semantic pointers (i.e., non-lexical pointers).
     * To obtain lexical pointers, call getRelatedFor on the appropriate object.
     * If there are no such synsets, this method returns the empty list.
     *
     * @param ptr the pointer for which related synsets are to be retrieved.
     * @return the list of synsets related by the specified pointer; if there are no such synsets, returns the empty list
     */
    fun getRelatedFor(ptr: Pointer): List<SynsetID> {
        return related[ptr] ?: emptyList()
    }

    /**
     * List of the ids of all synsets that are related to this synset
     */
    val allRelated: List<SynsetID>
        get() = related.values
            .flatMap { it.toList() }
            .distinct()
            .toList()

    /**
     * A word builder used to construct word objects inside the synset object constructor.
     */
    interface IWordBuilder {

        /**
         * Creates the word represented by this builder.
         * If the builder represents invalid values for a word, this method may throw an exception.
         *
         * @param synset the synset to which this word should be attached
         * @return the created word
         */
        fun toWord(synset: Synset): Sense
    }

    /**
     * Holds information about word objects before they are instantiated.
     *
     * Constructs a new word builder object. The constructor does not check
     * its arguments - this is done when the word is created.
     *
     * @property number the word number
     * @property lemma the lemma
     * @property lexID the id of the lexical file in which the word is listed
     * @property marker the adjective marker for the word
     */
    data class WordBuilder(
        private val number: Int,
        private val lemma: String,
        private val lexID: Int,
        private val marker: AdjMarker?,
    ) : IWordBuilder {

        init {
            checkWordNumber(number)
        }

        private val relatedWords: MutableMap<Pointer, MutableList<ISenseID>> = HashMap<Pointer, MutableList<ISenseID>>()

        private val verbFrames = ArrayList<VerbFrame>()

        override fun toWord(synset: Synset): Sense {
            return Sense(synset, SenseIDWithLemmaAndNum(synset.iD, number, lemma), lexID, marker, verbFrames, relatedWords)
        }

        fun addRelatedWord(ptrType: Pointer, id: ISenseID) {
            val words = relatedWords.computeIfAbsent(ptrType) { k: Pointer -> ArrayList<ISenseID>() }
            words.add(id)
        }

        fun addVerbFrame(frame: VerbFrame) {
            verbFrames.add(frame)
        }
    }

    companion object {

        /**
         * Takes an integer in the closed range [0,99999999] and converts it into an eight decimal digit zero-filled string.
         * E.g., "1" becomes "00000001", "1234" becomes "00001234", and so on.
         * This is used for the generation of synset and word numbers.
         *
         * @param offset the offset to be converted
         * @return the zero-filled string representation of the offset
         * @throws IllegalArgumentException if the specified offset is not in the valid range of [0,99999999]
         */
        @JvmStatic
        fun zeroFillOffset(offset: Int): String {
            checkOffset(offset)
            return "%08d".format(offset)
        }

        /**
         * Throws an exception if the specified offset is not in the valid range of [0,99999999].
         *
         * @param offset the offset to be checked
         * @return the checked offset
         * @throws IllegalArgumentException if the specified offset is not in the valid range of [0,99999999]
         */
        @JvmStatic
        fun checkOffset(offset: Int): Int {
            require(isLegalOffset(offset)) { "'$offset' is not a valid offset; offsets must be in the closed range [0,99999999]" }
            return offset
        }

        /**
         * Returns true an exception if the specified offset is not in the valid range of [0,99999999].
         *
         * @param offset the offset to be checked
         * @return true if the specified offset is in the closed range [0, 99999999]; false otherwise.
         */
        fun isLegalOffset(offset: Int): Boolean {
            if (offset < 0)
                return false
            return offset <= 99999999
        }

        fun buildWords(wordBuilders: List<IWordBuilder>, synset: Synset): List<Sense> {
            return wordBuilders
                .map { it.toWord(synset) }
                .toList()
        }

        private fun normalizeRelated(related: Map<Pointer, List<SynsetID>>?): Map<Pointer, List<SynsetID>> {
            return related?.entries
                ?.filterNot { it.value.isEmpty() }
                ?.associate { it.key to it.value }
                ?: emptyMap()
        }
    }
}
