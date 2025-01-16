package edu.mit.jwi.morph

import edu.mit.jwi.item.IHasPOS

/**
 * A rule for deriving a stem (a.k.a., root or lemma) from a word.
 */
interface IStemmingRule : IHasPOS {

    val suffix: String

    val ending: String?

    /**
     * Returns the set of suffixes that should be ignored when applying this stemming rule.
     * This method may return an empty set.
     * The ignore set will not include the string returned by getSuffix.
     *
     * @return a non-null but possibly empty set of suffixes
     */
    val suffixIgnoreSet: Set<String>

    /**
     * Applies this rule to the given word, adding the specified suffix to the end of the returned string.
     * If the rule cannot be applied to the word, this method returns null.
     *
     * @param word the word to which the stemming rule should be applied.
     * @param suffix a suffix that should be appended to the root once it has been derived.
     * @return the root of the word, or null if the rule cannot be applied to this word
     */
    fun apply(word: String, suffix: String? = null): String?
}
