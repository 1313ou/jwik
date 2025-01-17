package edu.mit.jwi.data.compare

import edu.mit.jwi.item.asIndexLemma

/**
 * A comparator that captures the ordering of lines in Wordnet index files (e.g., `index.adv` or `adv.idx` files).
 * These files are ordered alphabetically.
 */
object IndexLineComparator : ILineComparator {

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

        // neither strings are comments, so extract the lemma from the beginnings of both and compare them as two strings.
        var cut1 = s1.indexOf(' ')
        if (cut1 == -1) {
            cut1 = s1.length
        }
        val lemma1 = s1.substring(0, cut1)

        var cut2 = s2.indexOf(' ')
        if (cut2 == -1) {
            cut2 = s2.length
        }
        val lemma2 = s2.substring(0, cut2)
        return compareLemmasAsIndexLemmas(lemma1, lemma2)
    }

    /**
     * Compare lemmas as index lemmas
     *
     * @param lemma1 lemma 1
     * @param lemma2 lemma 1
     * @return compare code
     */
    private fun compareLemmasAsIndexLemmas(lemma1: String, lemma2: String): Int {
        val lemma1 = lemma1.asIndexLemma()
        val lemma2 = lemma2.asIndexLemma()
        return lemma1.compareTo(lemma2)
    }
}
