package edu.mit.jwi.item

import edu.mit.jwi.item.LexFile.Companion.ADJ_ALL
import edu.mit.jwi.item.Sense.Companion.checkSenseNumber
import edu.mit.jwi.item.SenseIDWithLemma.Companion.UNKNOWN_NUMBER
import java.util.*

/**
 * Synset
 *
 * @property iD the synset id
 * @property lexicalFile the lexical file for this synset
 * @property isAdjectiveSatellite true if this object represents an adjective satellite synset; false otherwise
 * @property isAdjectiveHead true if this object represents an adjective head synset; false otherwise
 * @property gloss the gloss for this synset
 * @property senses the list of senses in this synset
 * @property related a map of related synset lists, indexed by pointer
 * @throws IllegalArgumentException if the sense list is empty, or both the adjective satellite and adjective head flags are set
 * @throws IllegalArgumentException if either the adjective satellite and adjective head flags are set, and the lexical file number is not zero
 */
class Synset internal constructor(
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
     * Members
     */
    val members: List<ISenseBuilder>,

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
     * The senses that reference the synset
     */
    val senses: List<Sense> by lazy {
        members
            .map { it.toSense(this) }
            .toList()
    }

    init {
        require(!(isAdjectiveSatellite && isAdjectiveHead))
        require(!((isAdjectiveSatellite || isAdjectiveHead) && lexicalFile.number != ADJ_ALL.number))
    }

    override fun hashCode(): Int {
        return Objects.hash(iD, senses, this@Synset.related, gloss, isAdjectiveSatellite)
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
        if (senses != other.senses) {
            return false
        }
        if (gloss != other.gloss) {
            return false
        }
        if (isAdjectiveSatellite != other.isAdjectiveSatellite) {
            return false
        }
        return this@Synset.related == other.related
    }

    override fun toString(): String {
        return "S-{${iD} [${senses.joinToString(separator = ", ")}]}"
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
        return this@Synset.related[ptr] ?: emptyList()
    }

    /**
     * List of the ids of all synsets that are related to this synset
     */
    val allRelated: List<SynsetID>
        get() = this@Synset.related.values
            .flatMap { it.toList() }
            .distinct()
            .toList()

    /**
     * A sense builder used to construct sense objects inside the synset object constructor.
     */
    interface ISenseBuilder {

        /**
         * Creates the sense represented by this builder.
         * If the builder represents invalid values for a sense, this method may throw an exception.
         *
         * @param synset the synset to which this sense should be attached
         * @return the created sense
         */
        fun toSense(synset: Synset): Sense
    }

    /**
     * Holds information about sense objects before they are instantiated.
     *
     * Constructs a new sense builder object.
     * The constructor does not check its arguments - this is done when the sense is created.
     *
     * @property number the sense number
     * @property lemma the lemma
     * @property lexicalID the id of the lexical file in which the sense is listed
     * @property adjMarker the adjective marker for the sense
     */
    data class Member(
        private val number: Int,
        private val lemma: String,
        internal val lexicalID: Int,
        internal val adjMarker: AdjMarker?,
    ) : ISenseBuilder {

        init {
            checkSenseNumber(number)
        }

        var related: Map<Pointer, List<SenseID>> = HashMap<Pointer, List<SenseID>>()

        var verbFrames: List<VerbFrame> = emptyList()

        override fun toSense(synset: Synset): Sense {
            return Sense(synset, SenseIDWithLemmaAndNum(synset.iD, number, lemma), lexicalID, adjMarker, verbFrames, related)
        }
    }

    /**
     * A sense, which in Wordnet is an index paired with a synset.
     *
     * Constructs a new sense object.
     *
     * @param synset the synset for the sense
     * @param iD the sense id; its lemma may not be empty or all whitespace
     * @param member memer
     * @throws IllegalArgumentException if the adjective marker is non-null and this is not an adjective
     */
    inner class Sense2(

        override val iD: SenseIDWithLemma,

        val member: Member

        ) : IHasPOS, IItem<SenseID> {

        val synset: Synset
            get() = this@Synset

        override val pOS: POS
            get() = iD.synsetID.pOS

        val senseKey: SenseKey
            get() = SenseKey(iD.lemma, member.lexicalID, synset)

        val verbFrames: List<VerbFrame>
            get() = member.verbFrames

        val related: Map<Pointer, List<SenseID>>
            get() = member.related

        val allRelated: List<SenseID>
            get() = related.values
                .flatMap { it.toList() }
                .distinct()
                .toList()

        val lemma: String
            get() = iD.lemma

        val lexicalID: Int
            get() = member.lexicalID

        val adjectiveMarker: AdjMarker?
            get() = member.adjMarker

        init {
            checkLexicalID(member.lexicalID)
            require(!(synset.pOS !== POS.ADJECTIVE && adjectiveMarker != null))
        }

        override fun toString(): String {
            val sid = iD.synsetID.toString().substring(4)
            return when (iD) {
                is SenseIDWithLemmaAndNum -> "W-$sid-${iD.senseNumber}-${iD.lemma}"
                else                      -> "W-$sid-$UNKNOWN_NUMBER-${iD.lemma}"
            }
        }

        override fun hashCode(): Int {
            return Objects.hash(iD, lexicalID, adjectiveMarker, related, verbFrames)
        }

        override fun equals(obj: Any?): Boolean {
            // check nulls
            if (this === obj) {
                return true
            }
            if (obj == null) {
                return false
            }

            // check interface
            if (obj !is Sense) {
                return false
            }
            val that = obj

            // check id
            if (iD != that.iD) {
                return false
            }

            // check lexical id
            if (lexicalID != that.lexicalID) {
                return false
            }

            // check adjective marker
            if (adjectiveMarker == null) {
                if (that.adjectiveMarker != null) {
                    return false
                }
            } else if (adjectiveMarker != that.adjectiveMarker) {
                return false
            }

            // check maps
            if (verbFrames != that.verbFrames) {
                return false
            }
            return related == that.related
        }

        /**
         * Returns an immutable list of all sense ids related to this sense by the specified pointer type.
         * Note that this only returns senses related by lexical pointers (i.e., not semantic pointers).
         * To retrieve items related by semantic pointers, call getRelatedFor.
         * If this sense has no targets for the specified pointer, this method returns an empty list.
         * This method never returns null.
         *
         * @param ptr the pointer for which related senses are requested
         * @return the list of senses related by the specified pointer, or an empty list if none.
         */
        fun getRelatedFor(ptr: Pointer): List<SenseID> {
            return related[ptr] ?: emptyList<SenseID>()
        }

    }

    companion object {

        /**
         * Takes an integer in the closed range [0,99999999] and converts it into an eight decimal digit zero-filled string.
         * E.g., "1" becomes "00000001", "1234" becomes "00001234", and so on.
         * This is used for the generation of synset and sense numbers.
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

        internal fun normalizeRelated(related: Map<Pointer, List<SynsetID>>?): Map<Pointer, List<SynsetID>> {
            return related?.entries
                ?.filterNot { it.value.isEmpty() }
                ?.associate { it.key to it.value }
                ?: emptyMap()
        }

        /**
         * Checks the specified sense number, and throws an IllegalArgumentException if it is not legal.
         *
         * @param num the number to check
         * @throws IllegalArgumentException if the specified lexical id is not in the closed range [0,15]
         */
        @JvmStatic
        fun checkSenseNumber(num: Int) {
            require(!isIllegalSenseNumber(num)) { "'$num is an illegal sense number: sense numbers are in the closed range [1,255]" }
        }

        /**
         * Flag to check lexical IDs. Determines if lexical IDs are checked to be in the closed range [0,15]
         */
        var checkLexicalId: Boolean = false

        /**
         * Checks the specified lexical id, and throws an IllegalArgumentException if it is not legal.
         *
         * @param id the id to check
         * @throws IllegalArgumentException if the specified lexical id is not in the closed range [0,15]
         */
        @JvmStatic
        fun checkLexicalID(id: Int) {
            require(!(checkLexicalId && isIllegalLexicalID(id))) { "'$id is an illegal lexical id: lexical ids are in the closed range [0,15]" }
        }

        /**
         * Lexical ids are always an integer in the closed range [0,15].
         * In the Wordnet data files, lexical ids are represented as a one digit hexadecimal integer.
         *
         * @param id the lexical id to check
         * @return true if the specified integer is an invalid lexical id; false otherwise.
         */
        @JvmStatic
        fun isIllegalLexicalID(id: Int): Boolean {
            if (id < 0) {
                return true
            }
            return id > 15
        }

        /**
         * Sense numbers are always an integer in the closed range [1,255].
         * In the Wordnet data files, the sense number is determined by the order of the member lemma list.
         *
         * @param num the number to check
         * @return true if the specified integer is an invalid lexical id; false otherwise.
         */
        @JvmStatic
        fun isIllegalSenseNumber(num: Int): Boolean {
            if (num < 1) {
                return true
            }
            return num > 255
        }

        /**
         * Returns a string form of the lexical id as they are written in data files, which is a single digit hex number.
         *
         * @param lexID the lexical id to convert
         * @return a string form of the lexical id as they are written in data files, which is a single digit hex number.
         * @throws IllegalArgumentException if the specified integer is not a valid lexical id.
         */
        @JvmStatic
        fun getLexicalIDForDataFile(lexID: Int): String {
            checkLexicalID(lexID)
            return Integer.toHexString(lexID)
        }

        private val lexIDNumStrs = arrayOf("00", "01", "02", "03", "04", "05", "06", "07", "08", "09")

        /**
         * Returns a string form of the lexical id as they are written in sense keys, which is as a two-digit decimal number.
         *
         * @param lexID the lexical id to convert
         * @return a string form of the lexical id as they are written in sense keys, which is as a two-digit decimal number.
         * @throws IllegalArgumentException if the specified integer is not a valid lexical id.
         */
        @JvmStatic
        fun getLexicalIDForSenseKey(lexID: Int): String {
            checkLexicalID(lexID)
            return if (lexID < 10) lexIDNumStrs[lexID] else lexID.toString()
        }

        /**
         * Returns a string representation of the specified integer as a two hex digit zero-filled string.
         * E.g., "1" becomes "01", "10" becomes "0A", and so on.
         * This is used for the generation of Sense ID numbers.
         *
         * @param num the number to be converted
         * @return a two hex digit zero-filled string representing the specified number
         * @throws IllegalArgumentException if the specified number is not a legal sense number
         */
        @JvmStatic
        fun zeroFillSenseNumber(num: Int): String {
            return "%02x".format(num)
        }

        @JvmStatic
        private fun normalizeRelated(related: Map<Pointer, List<SenseID>>?): Map<Pointer, List<SenseID>> {
            return related?.entries
                ?.filterNot { it.value.isEmpty() }
                ?.associate { it.key to it.value }
                ?: emptyMap()
        }
    }
}
