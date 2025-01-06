package edu.mit.jwi.data.compare;

import edu.mit.jwi.NonNull;

public class Comparators
{
    /**
     * Case-sensitive index processing.
     */
    public static class CaseSensitiveIndexLineComparator extends IndexLineComparator
    {
        private static final CaseSensitiveIndexLineComparator INSTANCE = new CaseSensitiveIndexLineComparator();

        @NonNull
        public static CaseSensitiveIndexLineComparator getInstance()
        {
            return INSTANCE;
        }

        protected CaseSensitiveIndexLineComparator()
        {
            super(CommentComparator.getInstance());
        }

        @Override
        protected int compareLemmas(@NonNull String lemma1, @NonNull String lemma2)
        {
            return lemma1.compareTo(lemma2);
        }
    }

    public static class CaseSensitiveSenseKeyLineComparator extends SenseKeyLineComparator
    {
        private static final CaseSensitiveSenseKeyLineComparator INSTANCE = new CaseSensitiveSenseKeyLineComparator();

        @NonNull
        public static CaseSensitiveSenseKeyLineComparator getInstance()
        {
            return INSTANCE;
        }

        protected CaseSensitiveSenseKeyLineComparator()
        {
            super();
        }

        @Override
        protected int compareSenseKeys(@NonNull String senseKey1, @NonNull String senseKey2)
        {
            return senseKey1.compareTo(senseKey2);
        }
    }

    /**
     * Like ignore case, but in case of ignore-case equals, further case-sensitive processing
     * comparison is attempted.
     */
    public static class LexicographicOrderSenseKeyLineComparator extends SenseKeyLineComparator
    {
        private static final LexicographicOrderSenseKeyLineComparator INSTANCE = new LexicographicOrderSenseKeyLineComparator();

        @NonNull
        public static LexicographicOrderSenseKeyLineComparator getInstance()
        {
            return INSTANCE;
        }

        protected LexicographicOrderSenseKeyLineComparator()
        {
            super();
        }

        @Override
        protected int compareSenseKeys(@NonNull String senseKey1, @NonNull String senseKey2)
        {
            int c = senseKey1.compareToIgnoreCase(senseKey2);
            if (c != 0)
            {
                return c;
            }
            return -senseKey1.compareTo(senseKey2);
        }
    }
}
