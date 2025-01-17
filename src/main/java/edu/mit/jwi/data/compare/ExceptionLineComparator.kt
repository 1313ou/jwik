package edu.mit.jwi.data.compare

import edu.mit.jwi.data.parse.MisformattedLineException

/**
 * A comparator that captures the ordering of lines in Wordnet exception files (e.g., `exc.adv` or `adv.exc` files).
 * These files are ordered alphabetically.
 */
object ExceptionLineComparator : ILineComparator {

    override var commentDetector: ICommentDetector? = null

    override fun compare(line1: String, line2: String): Int {
        val tokens1 = SEPARATOR.split(line1)
        if (tokens1.isEmpty()) {
            throw MisformattedLineException(line1)
        }
        val tokens2 = SEPARATOR.split(line2)
        if (tokens2.isEmpty()) {
            throw MisformattedLineException(line2)
        }
        return tokens1[0].compareTo(tokens2[0])
    }

    private val SEPARATOR: Regex = " ".toRegex()
}
