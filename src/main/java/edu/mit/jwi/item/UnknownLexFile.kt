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

/**
 * Represents 'unknown' lexical files. This class implements internal caching,
 * much like the [Integer] class. Clients should use the static
 * [.getUnknownLexicalFile] method to retrieve instances of this
 * class.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.1.4
 */
class UnknownLexFile
/**
 * Obtain instances of this class via the static
 * [.getUnknownLexicalFile] method. This constructor is marked
 * protected so that the class may be sub-classed, but not directly
 * instantiated.
 *
 * @param num the number of the lexical file
 * @since JWI 2.1.4
 */
private constructor(num: Int) : LexFile(num, "Unknown", "Unknown Lexical File", null) {

    companion object {

        // cache for unknown lexical file objects.
        private val lexFileMap: MutableMap<Int?, UnknownLexFile?> = HashMap<Int?, UnknownLexFile?>()

        /**
         * Allows retrieval of an unknown lexical file object given the number.
         *
         * @param num the number of the lexical file
         * @return UnknownLexFile the unknown lexical file object corresponding to
         * the specified number
         * @throws IllegalArgumentException if the specified integer is not a valid lexical file number
         * @since JWI 2.1.4
         */
        @JvmStatic
        fun getUnknownLexicalFile(num: Int): UnknownLexFile {
            checkLexicalFileNumber(num)
            var result: UnknownLexFile? = lexFileMap[num]
            if (result == null) {
                result = UnknownLexFile(num)
                lexFileMap.put(num, result)
            }
            return result
        }
    }
}
