/* ******************************************************************************
 * Java Wordnet Interface Library (JWI) v2.4.0
 * Copyright (c) 2007-2015 Mark A. Finlayson
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 4.0
 * International Public License, which means it may be freely used for all
 * purposes, as long as proper acknowledgment is made.  See the license file
 * included with this distribution for more details.
 *******************************************************************************/
package edu.mit.jwi.data.compare

import java.util.*

/**
 * A comparator that captures the ordering of lines in Wordnet index files
 * (e.g., `index.adv` or `adv.idx` files). These files are
 * ordered alphabetically.
 *
 * This class follows a singleton design pattern, and is not intended to be
 * instantiated directly; rather, call the [.getInstance] method to get
 * the singleton instance.
 *
 * This constructor is marked protected so that the class may be
 * sub-classed, but not directly instantiated. Obtain instances of this
 * class via the static [.getInstance] method.
 *
 * @param detector the comment detector for this line comparator, or
 * null if there is none
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
open class IndexLineComparator(override val commentProcessor: CommentProcessor) : ILineComparator {

    override fun compare(s1: String, s2: String): Int {
        // check for comments
        val c1 = commentProcessor.isCommentLine(s1)
        val c2 = commentProcessor.isCommentLine(s2)

        if (c1 and c2) {
            // both lines are comments, defer to comment comparator
            return commentProcessor.compare(s1, s2)
        } else if (c1 and !c2) {
            // first line is a comment, should come before the other
            return -1
        } else if (c2) {
            // second line is a comment, should come before the other
            return 1
        }

        // Neither strings are comments, so extract the lemma from the
        // beginnings of both and compare them as two strings.
        var i1 = s1.indexOf(' ')
        var i2 = s2.indexOf(' ')
        if (i1 == -1) {
            i1 = s1.length
        }
        if (i2 == -1) {
            i2 = s2.length
        }

        val sub1 = s1.substring(0, i1)
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
        var lemma1 = lemma1
        var lemma2 = lemma2
        lemma1 = lemma1.lowercase()
        lemma2 = lemma2.lowercase()
        return lemma1.compareTo(lemma2)
    }

    object INSTANCE : IndexLineComparator(CommentProcessor)
}
