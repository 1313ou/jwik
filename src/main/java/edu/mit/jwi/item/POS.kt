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

import java.util.*

/**
 * Represents part of speech objects.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.0.0
 */
enum class POS
    (
    private val posName: String,

    /**
     * The tag that is used to indicate this part of speech in Wordnet data
     * files
     *
     * @return the character representing this part of speech
     * @since JWI 2.0.0
     */
     val tag: Char,

    /**
     * Returns the standard WordNet number of this part of speech
     *
     * @return the standard WordNet number of this part of speech
     * @since JWI 2.0.0
     */
    val number: Int,

    vararg patterns: String,
) {

    /**
     * Object representing the Noun part of speech.
     *
     * @since JWI 2.0.0
     */
    NOUN("noun", 'n', 1, "noun"),

    /**
     * Object representing the Verb part of speech.
     *
     * @since JWI 2.0.0
     */
    VERB("verb", 'v', 2, "verb"),

    /**
     * Object representing the Adjective part of speech.
     *
     * @since JWI 2.0.0
     */
    ADJECTIVE("adjective", 'a', 3, "adj", "adjective"),

    /**
     * Object representing the Adverb part of speech.
     *
     * @since JWI 2.0.0
     */
    ADVERB("adverb", 'r', 4, "adv", "adverb");

    /**
     * Returns a set of strings that can be used to identify resource
     * corresponding to objects with this part of speech.
     *
     * @return an immutable set of resource name hints
     * @since JWI 2.2
     */
    val resourceNameHints: Set<String> = Collections.unmodifiableSet<String>(HashSet<String>(listOf<String>(*patterns)))

    override fun toString(): String {
        return posName
    }

    companion object {

        // standard WordNet numbering scheme for parts of speech
        const val NUM_NOUN: Int = 1
        const val NUM_VERB: Int = 2
        const val NUM_ADJECTIVE: Int = 3
        const val NUM_ADVERB: Int = 4
        const val NUM_ADJECTIVE_SATELLITE: Int = 5

        // standard character tags for the parts of speech
        const val TAG_NOUN: Char = 'n'
        const val TAG_VERB: Char = 'v'
        const val TAG_ADJECTIVE: Char = 'a'
        const val TAG_ADVERB: Char = 'r'
        const val TAG_ADJECTIVE_SATELLITE: Char = 's'

        /**
         * Returns `true` if the specified number represents an adjective
         * satellite, namely, if the number is 5; `false` otherwise
         *
         * @param num the number to be checked
         * @return `true` if the specified number represents an adjective
         * satellite, namely, if the number is 5; `false` otherwise
         * @since JWI 2.0.0
         */
        @JvmStatic
        fun isAdjectiveSatellite(num: Int): Boolean {
            return num == 5
        }

        /**
         * Returns `true` if the specified character represents an
         * adjective satellite, namely, if the number is 's' or 'S';
         * `false` otherwise
         *
         * @param tag the character to be checked
         * @return `true` if the specified number represents an adjective
         * satellite, namely, if the number is 's' or 'S';
         * `false` otherwise
         * @since JWI 2.0.0
         */
        fun isAdjectiveSatellite(tag: Char): Boolean {
            return tag == 's' || tag == 'S'
        }

        /**
         * Retrieves the part of speech object given the number.
         *
         * @param num the number for the part of speech
         * @return POS the part of speech object corresponding to the specified tag,
         * or `null` if none is found
         * @since JWI 2.0.0
         */
        @JvmStatic
        
        fun getPartOfSpeech(num: Int): POS? {
            when (num) {
                (1)      -> return NOUN
                (2)      -> return VERB
                (4)      -> return ADVERB
                (5), (3) -> return ADJECTIVE
            }
            return null
        }

        /**
         * Retrieves of the part of speech object given the tag. Accepts both lower
         * and upper case characters.
         *
         * @param tag part of speech tag
         * @return POS the part of speech object corresponding to the specified tag,
         * or null if none is found
         * @since JWI 2.0.0
         */
        @JvmStatic
        fun getPartOfSpeech(tag: Char): POS {
            when (tag) {
                ('N'), ('n')               -> return NOUN
                ('V'), ('v')               -> return VERB
                ('R'), ('r')               -> return ADVERB
                ('S'), ('s'), ('A'), ('a') -> return ADJECTIVE
            }
            return throw IllegalArgumentException(tag.toString())
        }
    }
}
