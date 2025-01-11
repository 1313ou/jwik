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
 *
 * This constructor is marked protected so that the class may be
 * sub-classed, but not directly instantiated. Obtain instances of this
 * class via the static [.getInstance] method.
 *
 * @param detector the comment detector for this line comparator, or
 * null if there is none
 *
 * This class follows a singleton design pattern, and is not intended to be
 * instantiated directly; rather, call the [.getInstance] method to get
 * the singleton instance.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
class SearchIndexLineComparator private constructor(detector: CommentComparator) : IndexLineComparator(detector) {

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
        lemma1 = lemma1.lowercase(Locale.getDefault())
        lemma2 = lemma2.lowercase(Locale.getDefault())
        val l = min(lemma1.length.toDouble(), lemma2.length.toDouble()).toInt()
        return lemma1.substring(0, l).compareTo(lemma2.substring(0, l))
    }

    companion object {

        /**
         * Returns the singleton instance of this class, instantiating it if
         * necessary. The singleton instance will not be null.
         *
         * @return the non-null singleton instance of this class,
         * instantiating it if necessary.
         * @since JWI 2.0.0
         */
        var instance: SearchIndexLineComparator? = null
            get() {
                if (field == null) {
                    field = SearchIndexLineComparator(CommentComparator.instance!!)
                }
                return field
            }
            private set
    }
}
