package edu.mit.jwi.data.compare

import edu.mit.jwi.item.asIndexLemma
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
        val lemma1 = lemma1.asIndexLemma()
        val lemma2 = lemma2.asIndexLemma()
        val cut = min(lemma1.length.toInt(), lemma2.length.toInt())
        return lemma1.substring(0, cut).compareTo(lemma2.substring(0, cut))
    }
}
