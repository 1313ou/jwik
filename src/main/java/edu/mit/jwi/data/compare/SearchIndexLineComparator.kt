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
import kotlin.math.min

/**
 * A comparator that captures the ordering of lines in Wordnet index files
 * (e.g., `index.adv` or `adv.idx` files). These files are
 * ordered alphabetically.
 */
object SearchIndexLineComparator : IndexLineComparator(CommentProcessor) {

    /**
     * Compare lemmas (overridable if non-standard compare is needed)
     *
     * @param lemma1 lemma 1
     * @param lemma2 lemma 1
     * @return compare code
     */
    override fun compareLemmas(lemma1: String, lemma2: String): Int {
        var lemma1 = lemma1
        var lemma2 = lemma2
        lemma1 = lemma1.lowercase()
        lemma2 = lemma2.lowercase()
        val l = min(lemma1.length.toDouble(), lemma2.length.toDouble()).toInt()
        return lemma1.substring(0, l).compareTo(lemma2.substring(0, l))
    }
}
