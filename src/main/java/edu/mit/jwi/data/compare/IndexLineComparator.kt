package edu.mit.jwi.data.compare

/**
 * A comparator that captures the ordering of lines in Wordnet index files (e.g., `index.adv` or `adv.idx` files). These files are ordered alphabetically.
 */
open class BaseIndexLineComparator() : ILineComparator {

    override var commentDetector: ICommentDetector? = CommentProcessor

    override fun compare(s1: String, s2: String): Int {
        // check for comments
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

        // Neither strings are comments, so extract the lemma from the beginnings of both and compare them as two strings.
        var i1 = s1.indexOf(' ')
        if (i1 == -1) {
            i1 = s1.length
        }
        val sub1 = s1.substring(0, i1)

        var i2 = s2.indexOf(' ')
        if (i2 == -1) {
            i2 = s2.length
        }
        val sub2 = s2.substring(0, i2)
        return compareLemmas(sub1, sub2)
    }

    /**
     * Compare lemmas (overridable if non-standard compare is needed)
     *
     * @param lemma1 lemma 1
     * @param lemma2 lemma 1
     * @return compare code
     */
    protected open fun compareLemmas(lemma1: String, lemma2: String): Int {
        // TODO
        val lemma1 = lemma1.lowercase()
        val lemma2 = lemma2.lowercase()
        return lemma1.compareTo(lemma2)
    }
}

object IndexLineComparator: BaseIndexLineComparator()

