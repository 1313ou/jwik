package edu.mit.jwi.item

import java.io.Serializable
import java.util.*

/**
 * This class includes, as public fields, all pointers, lexical and semantic, defined in the standard WordNet distribution.
 *
 * This class in not implemented as an `Enum` so that clients may instantiate their own pointers using this implementation.
 */
class Pointer(
    symbol: String,
    name: String,
) : Serializable {

    val symbol: String = checkString(symbol)

    val name: String = checkString(name)

    private val toString: String = name.lowercase().replace(' ', '_').replace(",", "")

    override fun toString(): String {
        return toString
    }

    /**
     * This utility method implements the appropriate deserialization for this object.
     *
     * @return the appropriate deserialized object.
     */
    private fun readResolve(): Any {
        // check and see if this symbol matches DERIVED_FROM_ADJ (which is
        // excluded from the pointer map because it is ambiguous)
        if (DERIVED_FROM_ADJ.symbol == symbol && DERIVED_FROM_ADJ.name == name) {
            return DERIVED_FROM_ADJ
        }

        // otherwise, try to find a match symbol
        val pointer: Pointer? = pointerMap[symbol]
        if (pointer != null && pointer.symbol == symbol && pointer.name == name) {
            return pointer
        }

        // nothing matches, just return the deserialized object
        return this
    }

    companion object {

        val ALSO_SEE: Pointer = Pointer("^", "Also See")
        val ANTONYM: Pointer = Pointer("!", "Antonym")
        val ATTRIBUTE: Pointer = Pointer("=", "Attribute")
        val CAUSE: Pointer = Pointer(">", "Cause")
        val DERIVATIONALLY_RELATED: Pointer = Pointer("+", "Derivationally related form")
        val DERIVED_FROM_ADJ: Pointer = Pointer("\\", "Derived from adjective")
        val DOMAIN: Pointer = Pointer(";", "Domain of synset (undifferentiated)")
        val ENTAILMENT: Pointer = Pointer("*", "Entailment")
        val HYPERNYM: Pointer = Pointer("@", "Hypernym")
        val HYPERNYM_INSTANCE: Pointer = Pointer("@i", "Instance hypernym")
        val HYPONYM: Pointer = Pointer("~", "Hyponym")
        val HYPONYM_INSTANCE: Pointer = Pointer("~i", "Instance hyponym")
        val HOLONYM_MEMBER: Pointer = Pointer("#m", "Member holonym")
        val HOLONYM_SUBSTANCE: Pointer = Pointer("#s", "Substance holonym")
        val HOLONYM_PART: Pointer = Pointer("#p", "Part holonym")
        val MEMBER: Pointer = Pointer("-", "Member of this domain (undifferentiated)")
        val MERONYM_MEMBER: Pointer = Pointer("%m", "Member meronym")
        val MERONYM_SUBSTANCE: Pointer = Pointer("%s", "Substance meronym")
        val MERONYM_PART: Pointer = Pointer("%p", "Part meronym")
        val PARTICIPLE: Pointer = Pointer("<", "Participle")
        val PERTAINYM: Pointer = Pointer("\\", "Pertainym (pertains to nouns)")
        val REGION: Pointer = Pointer(";r", "Domain of synset - REGION")
        val REGION_MEMBER: Pointer = Pointer("-r", "Member of this domain - REGION")
        val SIMILAR_TO: Pointer = Pointer("&", "Similar To")
        val TOPIC: Pointer = Pointer(";c", "Domain of synset - TOPIC")
        val TOPIC_MEMBER: Pointer = Pointer("-c", "Member of this domain - TOPIC")
        val USAGE: Pointer = Pointer(";u", "Domain of synset - USAGE")
        val USAGE_MEMBER: Pointer = Pointer("-u", "Member of this domain - USAGE")
        val VERB_GROUP: Pointer = Pointer("$", "Verb Group")
        val IS_CAUSED: Pointer = Pointer(">^", "Is caused by")
        val IS_ENTAILED: Pointer = Pointer("*^", "Is entailed by")
        val COLLOCATION: Pointer = Pointer("`", "Collocation")

        /**
         * Throws an exception if the specified string empty, or all whitespace. Returns a trimmed form of the string.
         *
         * @param str the string to be checked
         * @return a trimmed form of the string
         * @throws IllegalArgumentException if the specified string is empty or all whitespace
         */
        private fun checkString(str: String): String {
            var str = str.trim { it <= ' ' }
            require(str.isNotEmpty())
            return str
        }

        private val pointerMap: Map<String, Pointer>

        private val pointerSet: Set<Pointer>

        init {
            val s = setOf(
                ALSO_SEE,
                ANTONYM,
                ATTRIBUTE,
                CAUSE,
                DERIVATIONALLY_RELATED,
                DERIVED_FROM_ADJ,
                DOMAIN,
                ENTAILMENT,
                HYPERNYM,
                HYPERNYM_INSTANCE,
                HYPONYM,
                HYPONYM_INSTANCE,
                HOLONYM_MEMBER,
                HOLONYM_SUBSTANCE,
                HOLONYM_PART,
                MEMBER,
                MERONYM_MEMBER,
                MERONYM_SUBSTANCE,
                MERONYM_PART,
                PARTICIPLE,
                PERTAINYM,
                REGION,
                REGION_MEMBER,
                SIMILAR_TO,
                TOPIC,
                TOPIC_MEMBER,
                USAGE,
                USAGE_MEMBER,
                VERB_GROUP,
                IS_CAUSED,
                IS_ENTAILED,
                COLLOCATION,
            )

            val m = s
                .asSequence()
                .filterNot { it == DERIVED_FROM_ADJ }
                .map { it.symbol to it }.toMap()

            pointerSet = Collections.unmodifiableSet<Pointer>(s)
            pointerMap = Collections.unmodifiableMap<String, Pointer>(m)
        }

        /**
         * Emulates the `Enum#values()` function. Returns an unmodifiable collection of all the pointers declared in this class, in the order they are declared.
         *
         * @return returns an unmodifiable collection of the pointers declared in this class
         */
        fun values(): Collection<Pointer> {
            return pointerSet
        }

        private const val AMBIGUOUS_SYMBOL = "\\"

        /**
         * Returns the pointer type (static final instance) that matches the
         * specified pointer symbol.
         *
         * @param symbol the symbol to look up
         * @param pos the part of speech for the symbol
         * except for ambiguous symbols @return pointer
         * @throws IllegalArgumentException if the symbol does not correspond to a known pointer.
         */
        @JvmStatic
        fun getPointerType(symbol: String, pos: POS?): Pointer {
            if (pos == POS.ADVERB && symbol == AMBIGUOUS_SYMBOL) {
                return DERIVED_FROM_ADJ
            }
            val pointerType: Pointer = pointerMap[symbol]!!
            requireNotNull(pointerType) { "No pointer type corresponding to symbol '$symbol'" }
            return pointerType
        }
    }
}
