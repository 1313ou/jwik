package edu.mit.jwi.data.compare

import kotlin.math.min

/**
 * A comparator that captures the ordering of lines in Wordnet index files (e.g., `index.adv` or `adv.idx` files).
 * These files are ordered alphabetically.
 */
object SearchIndexLineComparator : BaseIndexLineComparator() {

    /**
     * Compare lemmas (overridable if non-standard compare is needed)
     *
     * @param lemma1 lemma 1
     * @param lemma2 lemma 1
     * @return compare code
     */
    override fun compareLemmas(lemma1: String, lemma2: String): Int {
        // TODO
        val lemma1 = lemma1.lowercase()
        val lemma2 = lemma2.lowercase()
        val s = min(lemma1.length.toInt(), lemma2.length.toInt())
        return lemma1.substring(0, s).compareTo(lemma2.substring(0, s))
    }
}
