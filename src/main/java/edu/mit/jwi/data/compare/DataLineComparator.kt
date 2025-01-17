package edu.mit.jwi.data.compare

/**
 * A line comparator that captures the ordering of lines in Wordnet data files (e.g., `data.adv` or `adv.dat` files).
 * These files are ordered by offset, which is an eight-digit zero-filled decimal number that is assumed to start the line.
 */
object DataLineComparator : ILineComparator {

    override var commentDetector: ICommentDetector? = CommentProcessor

    override fun compare(s1: String, s2: String): Int {
        val c1 = CommentProcessor.isCommentLine(s1)
        val c2 = CommentProcessor.isCommentLine(s2)

        if (c1 and c2) {
            // both lines are comments, defer to comment comparator
            return CommentProcessor.compare(s1, s2)
        } else if (c1 and !c2) {
            // first line is a comment, should come before the other
            return -1
        } else if (c2) {
            // second line is a comment, should come before the other
            return 1
        }

        // neither strings are comments, so extract the offset from the beginnings of both and compare them as two ints.
        var cut1 = s1.indexOf(' ')
        if (cut1 == -1) {
            cut1 = s1.length
        }
        val offset1 = s1.substring(0, cut1).toInt()

        var cut2 = s2.indexOf(' ')
        if (cut2 == -1) {
            cut2 = s2.length
        }
        val offset2 = s2.substring(0, cut2).toInt()

        if (offset1 < offset2) {
            return -1
        } else if (offset1 > offset2) {
            return 1
        }
        return 0
    }
}
