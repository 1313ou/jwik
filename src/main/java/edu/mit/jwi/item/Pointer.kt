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
 * Concrete implementation of the `IPointer` interface. This class
 * includes, as public fields, all pointers, lexical and semantic, defined in
 * the standard WordNet distribution.
 *
 *
 * This class in not implemented as an `Enum` so that clients may
 * instantiate their own pointers using this implementation.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.1.0
 */
class Pointer(symbol: String, name: String) : IPointer {

    override val symbol: String = checkString(symbol)

    override val name: String = checkString(name)

    private val toString: String = name.lowercase(Locale.getDefault()).replace(' ', '_').replace(",", "")

    @NonNull
    override fun toString(): String {
        return toString
    }

    /**
     * This utility method implements the appropriate deserialization for this
     * object.
     *
     * @return the appropriate deserialized object.
     * @since JWI 2.4.0
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
        @JvmField
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
         * Throws an exception if the specified string is `null`, empty,
         * or all whitespace. Returns a trimmed form of the string.
         *
         * @param str the string to be checked
         * @return a trimmed form of the string
         * @throws NullPointerException     if the specified string is `null`
         * @throws IllegalArgumentException if the specified string is empty or all whitespace
         * @since JWI 2.2.0
         */
        private fun checkString(str: String): String {
            var str = str
            str = str.trim { it <= ' ' }
            require(str.isNotEmpty())
            return str
        }

        @NonNull
        private val pointerMap: MutableMap<String?, Pointer>
        @NonNull
        private val pointerSet: MutableSet<Pointer?>

        // class initialization code
        init {
            // get the instance fields
            val fields = Pointer::class.java.getFields()
            val instanceFields: MutableList<Field> = ArrayList<Field>()
            for (field in fields) {
                if (field.genericType === Pointer::class.java) {
                    instanceFields.add(field)
                }
            }

            // these are our backing collections
            val hiddenSet: MutableSet<Pointer?> = LinkedHashSet<Pointer?>(instanceFields.size)
            val hiddenMap: MutableMap<String?, Pointer?> = LinkedHashMap<String?, Pointer?>(instanceFields.size - 1)

            var ptr: Pointer?
            for (field in instanceFields) {
                try {
                    ptr = field.get(null) as Pointer?
                    if (ptr == null) {
                        continue
                    }
                    hiddenSet.add(ptr)
                    if (ptr !== DERIVED_FROM_ADJ) {
                        hiddenMap.put(ptr.symbol, ptr)
                    }
                } catch (_: IllegalAccessException) {
                    // Ignore
                }
            }

            // make the collections unmodifiable
            pointerSet = Collections.unmodifiableSet<Pointer?>(hiddenSet)
            pointerMap = Collections.unmodifiableMap<String?, Pointer?>(hiddenMap)
        }

        /**
         * Emulates the `Enum#values()` function. Returns an unmodifiable collection
         * of all the pointers declared in this class, in the order they are
         * declared.
         *
         * @return returns an unmodifiable collection of the pointers declared in
         * this class
         * @since JWI 2.1.0
         */
        @NonNull
        fun values(): MutableCollection<Pointer?> {
            return pointerSet
        }

        private const val AMBIGUOUS_SYMBOL = "\\"

        /**
         * Returns the pointer type (static final instance) that matches the
         * specified pointer symbol.
         *
         * @param symbol the symbol to look up
         * @param pos    the part of speech for the symbol; may be `null`
         * except for ambiguous symbols
         * @return pointer
         * @throws IllegalArgumentException if the symbol does not correspond to a known pointer.
         * @since JWI 2.1.0
         */
        @JvmStatic
        @Nullable
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
