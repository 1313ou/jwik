package edu.mit.jwi.morph

import edu.mit.jwi.item.IHasPOS
import edu.mit.jwi.item.POS

/**
 * A rule for deriving a stem (a.k.a., root or lemma) from a word.
 *
 * Creates a new stemming rule with the specified suffix, ending, and avoid set
 *
 * @param suffix the suffix that should be stripped from a word; should not be empty or all whitespace.
 * @param ending the ending that should be stripped from a word; may be empty or all whitespace.
 * @param pOS the part-of-speech to which this rule applies
 * @param ignore the set of suffixes that, when present, indicate this rule should not be applied. May not contain empties.
 */
class StemmingRule(
    suffix: String,
    ending: String,
    override val pOS: POS,
    vararg ignore: String,
) : IHasPOS {

    val suffix: String = suffix.trim { it <= ' ' }

    val ending: String = ending.trim { it <= ' ' }

    /**
     * The set of suffixes that should be ignored when applying this stemming rule.
     * Possibly an empty set of suffixes.
     * The ignore set will not include the suffix string.
     */
    val suffixIgnores = ignore.asSequence()
        .map { it.trim { it <= ' ' } }
        .onEach { require(it.isNotEmpty()) }
        .toSet()

    init {
        require(suffix.isNotEmpty())
        require(!suffixIgnores.contains(suffix))
    }

    /**
     * Applies this rule to the given word, adding the specified suffix to the end of the returned string.
     * If the rule cannot be applied to the word, this method returns null.
     *
     * @param word the word to which the stemming rule should be applied.
     * @param extraSuffix a suffix that should be appended to the root once it has been derived.
     * @return the root of the word, or null if the rule cannot be applied to this word
     */
    fun apply(word: String, extraSuffix: String? = null): String? {
        // does not apply if the suffix or any ignored suffix is already present
        if (!word.endsWith(suffix) || suffixIgnores.any { word.endsWith(it) }) {
            return null
        }

        // apply the rule
        val cut = word.length - suffix.length
        val root = word.substring(0, cut)
        return "$root$ending${extraSuffix ?: ""}"
    }
}
