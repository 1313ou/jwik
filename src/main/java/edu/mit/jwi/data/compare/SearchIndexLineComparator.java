/* ******************************************************************************
 * Java Wordnet Interface Library (JWI) v2.4.0
 * Copyright (c) 2007-2015 Mark A. Finlayson
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 4.0
 * International Public License, which means it may be freely used for all
 * purposes, as long as proper acknowledgment is made.  See the license file
 * included with this distribution for more details.
 *******************************************************************************/

package edu.mit.jwi.data.compare;

/**
 * <p>
 * A comparator that captures the ordering of lines in Wordnet index files
 * (e.g., <code>index.adv</code> or <code>adv.idx</code> files). These files are
 * ordered alphabetically.
 * </p>
 * <p>
 * This class follows a singleton design pattern, and is not intended to be
 * instantiated directly; rather, call the {@link #getInstance()} method to get
 * the singleton instance.
 * </p>
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
public class SearchIndexLineComparator extends IndexLineComparator
{
    // singleton instance
    private static SearchIndexLineComparator instance;

    /**
     * Returns the singleton instance of this class, instantiating it if
     * necessary. The singleton instance will not be <code>null</code>.
     *
     * @return the non-<code>null</code> singleton instance of this class,
     * instantiating it if necessary.
     * @since JWI 2.0.0
     */
    public static SearchIndexLineComparator getInstance()
    {
        if (instance == null)
        {
            instance = new SearchIndexLineComparator(CommentComparator.getInstance());
        }
        return instance;
    }


    /**
     * This constructor is marked protected so that the class may be
     * sub-classed, but not directly instantiated. Obtain instances of this
     * class via the static {@link #getInstance()} method.
     *
     * @param detector the comment detector for this line comparator, or
     *                 <code>null</code> if there is none
     * @throws NullPointerException if the specified comment comparator is <code>null</code>
     * @since JWI 1.0
     */
    protected SearchIndexLineComparator(CommentComparator detector)
    {
        super(detector);
    }

    /**
     * Compare lemmas (overridable if non-standard compare is needed)
     *
     * @param lemma1 lemma 1
     * @param lemma2 lemma 1
     * @return compare code
     */
    protected int compareLemmas(String lemma1, String lemma2)
    {
        lemma1 = lemma1.toLowerCase();
        lemma2 = lemma2.toLowerCase();
        int l = Math.min(lemma1.length(), lemma2.length());
        return lemma1.substring(0, l).compareTo(lemma2.substring(0, l));
    }
}
