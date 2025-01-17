package edu.mit.jwi.data.compare

interface ICommentDetector {

    fun isCommentLine(line: String): Boolean
}

/**
 * A detector for comment lines in data resources.
 * Also serve as comparators that say how comment lines are ordered, if at all.
 *
 * Default comment detector that is designed for comments found at the head of Wordnet dictionary files.
 * It assumes that each comment line starts with two spaces, followed by a number that indicates the position of the comment line relative to the rest of the comment lines in the file.
 */
object CommentProcessor : ICommentDetector, Comparator<String> {

    /**
     * Whether the specified string is a comment line
     *
     * @param line the line to be analyzed
     * @return true if the specified string is a comment line, false otherwise.
     */
    override fun isCommentLine(line: String): Boolean {
        return line.length >= 2 && line[0] == ' ' && line[1] == ' '
    }

    override fun compare(s1: String, s2: String): Int {
        var s1 = s1.trim { it <= ' ' }
        var cut1 = s1.indexOf(' ')
        if (cut1 == -1) {
            cut1 = s1.length
        }
        var s2 = s2.trim { it <= ' ' }
        var cut2 = s2.indexOf(' ')
        if (cut2 == -1) {
            cut2 = s2.length
        }

        val num1 = s1.substring(0, cut1).toInt()
        val num2 = s2.substring(0, cut2).toInt()
        if (num1 < num2) {
            return -1
        } else if (num1 > num2) {
            return 1
        }
        return 0
    }
}