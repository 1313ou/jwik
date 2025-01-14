package edu.mit.jwi.data.compare

/**
 * Case-sensitive index processing.
 */
object CaseSensitiveIndexLineComparator : BaseIndexLineComparator(CommentProcessor) {

    override fun compareLemmas(lemma1: String, lemma2: String): Int {
        return lemma1.compareTo(lemma2)
    }
}

/**
 * Case-sensitive sense key comparator
 */
object CaseSensitiveSenseKeyLineComparator : BaseSenseKeyLineComparator() {

    override fun compareSenseKeys(senseKey1: String, senseKey2: String): Int {
        return senseKey1.compareTo(senseKey2)
    }
}

/**
 * Like ignore case, but in case of ignore-case equals, further case-sensitive processing comparison is attempted.
 */
object LexicographicOrderSenseKeyLineComparator : BaseSenseKeyLineComparator() {

    override fun compareSenseKeys(senseKey1: String, senseKey2: String): Int {
        val c = senseKey1.compareTo(senseKey2, ignoreCase = true)
        if (c != 0) {
            return c
        }
        return -senseKey1.compareTo(senseKey2)
    }
}
