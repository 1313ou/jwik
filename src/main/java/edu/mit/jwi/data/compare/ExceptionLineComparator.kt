package edu.mit.jwi.data.compare

import edu.mit.jwi.data.parse.MisformattedLineException
import java.util.regex.Pattern

/**
 * A comparator that captures the ordering of lines in Wordnet exception files (e.g., `exc.adv` or `adv.exc` files).
 * These files are ordered alphabetically.
 */
object ExceptionLineComparator : ILineComparator {

    override var commentDetector: ICommentDetector? = null

    override fun compare(line1: String, line2: String): Int {
        val words1 = SEPARATOR.split(line1)
        if (words1.isEmpty()) {
            throw MisformattedLineException(line1)
        }
        val words2 = SEPARATOR.split(line2)
        if (words2.isEmpty()) {
            throw MisformattedLineException(line2)
        }
        return words1[0].compareTo(words2[0])
    }

    private val SEPARATOR: Pattern = Pattern.compile(" ")
}
