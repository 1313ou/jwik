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
import java.lang.reflect.Field
import java.util.*

/**
 * Concrete implementation of the `ILexFile` interface. This class
 * includes, as public fields, all lexical files defined in the standard WordNet
 * distribution.
 *
 * This class in not implemented as an `Enum` so that clients may
 * instantiate their own lexical file objects using this implementation.

 * Constructs a new lexical file description object.
 *
 * @param num  the lexical file number, in the closed range [0,99]
 * @param name the name of the lexical file, may not be `null`,
 * empty, or all whitespace
 * @param desc the description of the lexical file, may not be
 * `null`, empty, or all whitespace
 * @param pos  the part of speech for the lexical file, may be
 * `null`
 * @throws NullPointerException     if either the name or description are `null`
 * @throws IllegalArgumentException if either the name or description are empty or all whitespace
 * @since JWI 2.1.0
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.1.0
 */
open class LexFile(num: Int, name: String, desc: String, pos: POS?) : ILexFile {

    override val number: Int = checkLexicalFileNumber(num)

    override val name: String = checkString(name)

    override val pOS: POS? = pos

    private val desc: String = desc

    override val description: String
        get() = desc

    override fun toString(): String {
        return name
    }

    /**
      */

    /*
      * (non-Javadoc)
      *
      * @see java.lang.Object#hashCode()
      */
    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + (if (desc == null) 0 else desc.hashCode())
        result = prime * result + (if (name == null) 0 else name.hashCode())
        result = prime * result + this.number
        result = prime * result + (if (pOS == null) 0 else pOS.hashCode())
        return result
    }

    /* (non-Javadoc) @see java.lang.Object#equals(java.lang.Object) */
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
        val other = obj as ILexFile
        if (desc == null) {
            if (other.description != null) {
                return false
            }
        } else if (desc != other.description) {
            return false
        }
        if (name == null) {
            if (other.name != null) {
                return false
            }
        } else if (name != other.name) {
            return false
        }
        if (this.number != other.number) {
            return false
        }
        if (pOS == null) {
            return other.pOS == null
        } else {
            return pOS == other.pOS
        }
    }

    /**
     * This utility method implements the appropriate deserialization for this
     * object.
     *
     * @return the appropriate deserialized object.
     * @since JWI 2.4.0
     */
    @NonNull
    protected fun readResolve(): Any {
        val lexFile: LexFile = getLexicalFile(this.number)
        return if (this == lexFile) lexFile else this
    }

    companion object {

        val ADJ_ALL: LexFile = LexFile(0, "adj.all", "all adjective clusters", POS.ADJECTIVE)
        val ADJ_PERT: LexFile = LexFile(1, "adj.pert", "relational adjectives (pertainyms)", POS.ADJECTIVE)
        val ADV_ALL: LexFile = LexFile(2, "adv.all", "all adverbs", POS.ADVERB)
        val NOUN_TOPS: LexFile = LexFile(3, "noun.Tops", "unique beginner for nouns", POS.NOUN)
        val NOUN_ACT: LexFile = LexFile(4, "noun.act", "nouns denoting acts or actions", POS.NOUN)
        val NOUN_ANIMAL: LexFile = LexFile(5, "noun.animal", "nouns denoting animals", POS.NOUN)
        val NOUN_ARTIFACT: LexFile = LexFile(6, "noun.artifact", "nouns denoting man-made objects", POS.NOUN)
        val NOUN_ATTRIBUTE: LexFile = LexFile(7, "noun.attribute", "nouns denoting attributes of people and objects", POS.NOUN)
        val NOUN_BODY: LexFile = LexFile(8, "noun.body", "nouns denoting body parts", POS.NOUN)
        val NOUN_COGNITION: LexFile = LexFile(9, "noun.cognition", "nouns denoting cognitive processes and contents", POS.NOUN)
        val NOUN_COMMUNICATION: LexFile = LexFile(10, "noun.communication", "nouns denoting communicative processes and contents", POS.NOUN)
        val NOUN_EVENT: LexFile = LexFile(11, "noun.event", "nouns denoting natural events", POS.NOUN)
        val NOUN_FEELING: LexFile = LexFile(12, "noun.feeling", "nouns denoting feelings and emotions", POS.NOUN)
        val NOUN_FOOD: LexFile = LexFile(13, "noun.food", "nouns denoting foods and drinks", POS.NOUN)
        val NOUN_GROUP: LexFile = LexFile(14, "noun.group", "nouns denoting groupings of people or objects", POS.NOUN)
        val NOUN_LOCATION: LexFile = LexFile(15, "noun.location", "nouns denoting spatial position", POS.NOUN)
        val NOUN_MOTIVE: LexFile = LexFile(16, "noun.motive", "nouns denoting goals", POS.NOUN)
        val NOUN_OBJECT: LexFile = LexFile(17, "noun.object", "nouns denoting natural objects (not man-made)", POS.NOUN)
        val NOUN_PERSON: LexFile = LexFile(18, "noun.person", "nouns denoting people", POS.NOUN)
        val NOUN_PHENOMENON: LexFile = LexFile(19, "noun.phenomenon", "nouns denoting natural phenomena", POS.NOUN)
        val NOUN_PLANT: LexFile = LexFile(20, "noun.plant", "nouns denoting plants", POS.NOUN)
        val NOUN_POSSESSION: LexFile = LexFile(21, "noun.possession", "nouns denoting possession and transfer of possession", POS.NOUN)
        val NOUN_PROCESS: LexFile = LexFile(22, "noun.process", "nouns denoting natural processes", POS.NOUN)
        val NOUN_QUANTITY: LexFile = LexFile(23, "noun.quantity", "nouns denoting quantities and units of measure", POS.NOUN)
        val NOUN_RELATION: LexFile = LexFile(24, "noun.relation", "nouns denoting relations between people or things or ideas", POS.NOUN)
        val NOUN_SHAPE: LexFile = LexFile(25, "noun.shape", "nouns denoting two and three dimensional shapes", POS.NOUN)
        val NOUN_STATE: LexFile = LexFile(26, "noun.state", "nouns denoting natural processes", POS.NOUN)
        val NOUN_SUBSTANCE: LexFile = LexFile(27, "noun.substance", "nouns denoting substances", POS.NOUN)
        val NOUN_TIME: LexFile = LexFile(28, "noun.time", "nouns denoting time and temporal relations", POS.NOUN)
        val VERB_BODY: LexFile = LexFile(29, "verb.body", "verbs of grooming, dressing and bodily care", POS.VERB)
        val VERB_CHANGE: LexFile = LexFile(30, "verb.change", "verbs of size, temperature change, intensifying, etc.", POS.VERB)
        val VERB_COGNITION: LexFile = LexFile(31, "verb.cognition", "verbs of thinking, judging, analyzing, doubting", POS.VERB)
        val VERB_COMMUNICATION: LexFile = LexFile(32, "verb.communication", "verbs of telling, asking, ordering, singing", POS.VERB)
        val VERB_COMPETITION: LexFile = LexFile(33, "verb.competition", "verbs of fighting, athletic activities", POS.VERB)
        val VERB_CONSUMPTION: LexFile = LexFile(34, "verb.consumption", "verbs of eating and drinking", POS.VERB)
        val VERB_CONTACT: LexFile = LexFile(35, "verb.contact", "verbs of touching, hitting, tying, digging", POS.VERB)
        val VERB_CREATION: LexFile = LexFile(36, "verb.creation", "verbs of sewing, baking, painting, performing", POS.VERB)
        val VERB_EMOTION: LexFile = LexFile(37, "verb.emotion", "verbs of feeling", POS.VERB)
        val VERB_MOTION: LexFile = LexFile(38, "verb.motion", "verbs of walking, flying, swimming", POS.VERB)
        val VERB_PERCEPTION: LexFile = LexFile(39, "verb.perception", "verbs of seeing, hearing, feeling", POS.VERB)
        val VERB_POSSESSION: LexFile = LexFile(40, "verb.possession", "verbs of buying, selling, owning", POS.VERB)
        val VERB_SOCIAL: LexFile = LexFile(41, "verb.social", "verbs of political and social activities and events", POS.VERB)
        val VERB_STATIVE: LexFile = LexFile(42, "verb.stative", "verbs of being, having, spatial relations", POS.VERB)
        val VERB_WEATHER: LexFile = LexFile(43, "verb.weather", "verbs of raining, snowing, thawing, thundering", POS.VERB)
        val ADJ_PPL: LexFile = LexFile(44, "adj.ppl", "participial adjectives", POS.ADJECTIVE)

        /**
         * Throws an exception if the specified string is `null`, empty,
         * or all whitespace. Returns a trimmed form of the string.
         *
         * @param str the string to be checked
         * @return a trimmed form of the string
         * @throws NullPointerException     if the specified string is `null`
         * @throws IllegalArgumentException if the specified string is empty or all whitespace
         * @since JWI 2.2.0
         */
        protected fun checkString(str: String): String {
            var str = str
            str = str.trim { it <= ' ' }
            require(str.length != 0)
            return str
        }

        /**
         * Throws an exception if the specified lexical file number is not a valid
         * lexical file number
         *
         * @param num the number to be checked
         * @throws IllegalArgumentException if the specified lexical file number is not a legal lexical
         * file number
         * @since JWI 2.1.0
         */
        @JvmStatic
        fun checkLexicalFileNumber(num: Int): Int {
            require(!isIllegalLexicalFileNumber(num)) { "'$num is an illegal lexical file number: Lexical file numbers must be in the closed range [0,99]" }
            return num
        }

        /**
         * Returns `true` if the number represents a valid lexical file
         * number, namely, a number in the closed range [0, 99]; returns
         * `false` otherwise.
         *
         * @param num the number to be checked
         * @return `true` if the number represents a valid lexical file
         * number, namely, a number in the closed range [0, 99]; returns
         * `false` otherwise.
         * @since JWI 2.1.0
         */
        fun isIllegalLexicalFileNumber(num: Int): Boolean {
            return num < 0 || 99 < num
        }

        private val lexFileNumStrs = arrayOf<String>("00", "01", "02", "03", "04", "05", "06", "07", "08", "09")

        /**
         * Returns a two-character string representation of a lexical file number,
         * zero-filled if necessary. This string is suitable for inclusion in
         * Wordnet-formatted files.
         *
         * @param num the number to be represented
         * @return a two-character string representing the number
         * @throws IllegalArgumentException if the specified integer is not a legal lexical file number
         * @since JWI 2.1.0
         */
        fun getLexicalFileNumberString(num: Int): String {
            checkLexicalFileNumber(num)
            if (num < 10) {
                return lexFileNumStrs[num]
            }
            return num.toString()
        }

        @NonNull
        private val lexFileMap: MutableMap<Int?, LexFile>

        init {
            // get instance fields
            val fields = LexFile::class.java.getFields()
            val instanceFields: MutableList<Field> = ArrayList<Field>()
            for (field in fields) {
                if (field.getGenericType() === LexFile::class.java) {
                    instanceFields.add(field)
                }
            }

            // backing map
            val hidden: MutableMap<Int?, LexFile?> = LinkedHashMap<Int?, LexFile?>(instanceFields.size)

            // get instances
            var lexFile: LexFile?
            for (field in instanceFields) {
                try {
                    lexFile = field.get(null) as LexFile?
                    if (lexFile != null) {
                        hidden.put(lexFile.number, lexFile)
                    }
                } catch (e: IllegalAccessException) {
                    // Ignore
                }
            }

            // make backing map unmodifiable
            lexFileMap = Collections.unmodifiableMap<Int?, LexFile?>(hidden)
        }

        /**
         * Emulates the `Enum#values()` function. Returns an unmodifiable
         * collection of all the lexical file descriptions declared in this class,
         * in the order they are declared.
         *
         * @return returns an unmodifiable collection of the lexical file
         * description declared in this class
         * @since JWI 2.1.0
         */
        @NonNull
        fun values(): Collection<LexFile> {
            return lexFileMap.values
        }

        /**
         * A convenience method that allows retrieval of one of the built-in lexical
         * file descriptions given the number. If no such description exists, then
         * the method returns `null`.
         *
         * @param num the number for the lexical file object
         * @return ILexFile the lexical file corresponding to the specified tag, or
         * null if none is found
         * @since JWI 2.1.0
         */
        @JvmStatic
        @Nullable
        fun getLexicalFile(num: Int): LexFile {
            return lexFileMap[num]!!
        }
    }
}
