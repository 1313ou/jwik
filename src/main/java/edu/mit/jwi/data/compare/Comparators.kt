package edu.mit.jwi.data.compare

class Comparators {

    /**
     * Case-sensitive index processing.
     */
    class CaseSensitiveIndexLineComparator private constructor() : IndexLineComparator(CommentProcessor) {

        override fun compareLemmas(lemma1: String, lemma2: String): Int {
            return lemma1.compareTo(lemma2)
        }

        companion object {

            val instance: CaseSensitiveIndexLineComparator = CaseSensitiveIndexLineComparator()
        }
    }

    class CaseSensitiveSenseKeyLineComparator private constructor() : SenseKeyLineComparator() {

        override fun compareSenseKeys(senseKey1: String, senseKey2: String): Int {
            return senseKey1.compareTo(senseKey2)
        }

        companion object {

            val instance: CaseSensitiveSenseKeyLineComparator = CaseSensitiveSenseKeyLineComparator()
        }
    }

    /**
     * Like ignore case, but in case of ignore-case equals, further case-sensitive processing
     * comparison is attempted.
     */
    class LexicographicOrderSenseKeyLineComparator private constructor() : SenseKeyLineComparator() {

        override fun compareSenseKeys(senseKey1: String, senseKey2: String): Int {
            val c = senseKey1.compareTo(senseKey2, ignoreCase = true)
            if (c != 0) {
                return c
            }
            return -senseKey1.compareTo(senseKey2)
        }

        companion object {

            val instance: LexicographicOrderSenseKeyLineComparator = LexicographicOrderSenseKeyLineComparator()
        }
    }
}
