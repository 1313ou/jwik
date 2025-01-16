package edu.mit.jwi.item

/**
 * Represents 'unknown' lexical files.
 * This class implements internal caching, much like the Integer class.
 * Clients should use the getUnknownLexicalFile method to retrieve instances of this class.
 */
class UnknownLexFile
/**
 * Obtain instances of this class via the getUnknownLexicalFile] method.
 * This constructor is marked protected so that the class may be sub-classed, but not directly instantiated.
 *
 * @param num the number of the lexical file
 */
private constructor(num: Int) : LexFile(num, "Unknown", "Unknown Lexical File", null) {

    companion object {

        // cache for unknown lexical file objects.
        private val lexFileMap: MutableMap<Int?, UnknownLexFile?> = HashMap<Int?, UnknownLexFile?>()

        /**
         * Allows retrieval of an unknown lexical file object given the number.
         *
         * @param num the number of the lexical file
         * @return UnknownLexFile the unknown lexical file object corresponding to the specified number
         * @throws IllegalArgumentException if the specified integer is not a valid lexical file number
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
