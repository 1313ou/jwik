/* ******************************************************************************
 * Java Wordnet Interface Library (JWI) v2.4.0
 * Copyright (c) 2007-2015 Mark A. Finlayson
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 4.0
 * International Public License, which means it may be freely used for all
 * purposes, as long as proper acknowledgment is made.  See the license file
 * included with this distribution for more details.
 *******************************************************************************/
package edu.mit.jwi.morph

import edu.mit.jwi.item.IHasPOS

/**
 * A rule for deriving a stem (a.k.a., root or lemma) from a word.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.3.1
 */
interface IStemmingRule : IHasPOS {

    val suffix: String?

    val ending: String?

    /**
     * Returns the set of suffixes that should be ignored when applying this
     * stemming rule. This method will never return null, but it
     * may return an empty set. The ignore set will not include the string
     * returned by [.getSuffix].
     *
     * @return a non-null but possibly empty set of suffixes to be
     * ignored
     * @since JWI 2.3.1
     */
    val suffixIgnoreSet: Set<String>

    /**
     * Applies this rule to the given word. The word should not be
     * null, but may be empty. If the rule cannot be applied to the
     * word, this method returns null. This call is equivalent to
     * calling [.apply] with null as the
     * second argument
     *
     * @param word the word to which the stemming rule should be applied.
     * @return the root of the word, or null if the rule cannot be
     * applied to this word
     * @since JWI 2.3.1
     */
    fun apply(word: String): String?

    /**
     * Applies this rule to the given word, adding the specified suffix to the
     * end of the returned string. If the rule cannot be applied to the word,
     * this method returns null.
     *
     * @param word   the word to which the stemming rule should be applied.
     * @param suffix a suffix that should be appended to the root once it has been
     * derived; may be null.
     * @return the root of the word, or null if the rule cannot be
     * applied to this word
     * @since JWI 2.3.1
     */
    fun apply(word: String, suffix: String?): String?
}
