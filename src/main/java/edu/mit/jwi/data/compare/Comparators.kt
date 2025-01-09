package edu.mit.jwi.data.compare

import edu.mit.jwi.NonNull
import edu.mit.jwi.data.compare.IndexLineComparator

class Comparators {
    /**
     * Case-sensitive index processing.
     */
    class CaseSensitiveIndexLineComparator private constructor() : IndexLineComparator(CommentComparator.instance!!) {

        override fun compareLemmas(@NonNull lemma1: String, @NonNull lemma2: String): Int {
            return lemma1.compareTo(lemma2)
        }

        companion object {

            @get:NonNull
            val instance: CaseSensitiveIndexLineComparator = CaseSensitiveIndexLineComparator()
        }
    }

    class CaseSensitiveSenseKeyLineComparator private constructor() : SenseKeyLineComparator() {

        override fun compareSenseKeys(@NonNull senseKey1: String, @NonNull senseKey2: String): Int {
            return senseKey1.compareTo(senseKey2)
        }

        companion object {

            @get:NonNull
            val instance: CaseSensitiveSenseKeyLineComparator = CaseSensitiveSenseKeyLineComparator()
        }
    }

    /**
     * Like ignore case, but in case of ignore-case equals, further case-sensitive processing
     * comparison is attempted.
     */
    class LexicographicOrderSenseKeyLineComparator private constructor() : SenseKeyLineComparator() {

        override fun compareSenseKeys(@NonNull senseKey1: String, @NonNull senseKey2: String): Int {
            val c = senseKey1.compareTo(senseKey2, ignoreCase = true)
            if (c != 0) {
                return c
            }
            return -senseKey1.compareTo(senseKey2)
        }

        companion object {

            @get:NonNull
            val instance: LexicographicOrderSenseKeyLineComparator = LexicographicOrderSenseKeyLineComparator()
        }
    }
}
