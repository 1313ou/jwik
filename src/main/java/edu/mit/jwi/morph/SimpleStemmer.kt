package edu.mit.jwi.morph

import edu.mit.jwi.item.POS
import java.util.regex.Pattern
import kotlin.sequences.distinct

/**
 * Provides simple a simple pattern-based stemming facility based on the "Rules of Detachment" as described in the `morphy` man page in the Wordnet distribution, which can be found at [ http://wordnet.princeton.edu/man/morphy.7WN.html](http://wordnet.princeton.edu/man/morphy.7WN.html)
 * It also attempts to strip "ful" endings.
 * It does not search Wordnet to see if stems actually exist.
 * In particular, quoting from that man page:
 * <h3>Rules of Detachment</h3>
 * *
 * The following table shows the rules of detachment used by Morphy.
 * If a word ends with one of the suffixes, it is stripped from the word and the
 * corresponding ending is added.
 * No rules are applicable to adverbs.
 *
 *
 * POS Suffix Ending<br></br>
 *
 *  * NOUN "s" ""
 *  * NOUN "ses" "s"
 *  * NOUN "xes" "x"
 *  * NOUN "zes" "z"
 *  * NOUN "ches" "ch"
 *  * NOUN "shes" "sh"
 *  * NOUN "men" "man"
 *  * NOUN "ies" "y"
 *  * VERB "s" ""
 *  * VERB "ies" "y"
 *  * VERB "es" "e"
 *  * VERB "es" ""
 *  * VERB "ed" "e"
 *  * VERB "ed" ""
 *  * VERB "ing" "e"
 *  * VERB "ing" ""
 *  * ADJ "er" ""
 *  * ADJ "est" ""
 *  * ADJ "er" "e"
 *  * ADJ "est" "e"
 *
 * <h3>Special Processing for nouns ending with 'ful'</h3>
 *
 * Morphy contains code that searches for nouns ending with ful and performs a transformation on the substring preceding it.
 * It then appends 'ful' back onto the resulting string and returns it.
 * For example, if passed the nouns "boxesful", it will return "boxful".
 */
open class SimpleStemmer : IStemmer {

    /**
     * Returns a set of stemming rules used by this stemmer.
     * Will not return a null map, but it may be empty.
     * The lists in the map may be empty.
     *
     * @return the rule map for this stemmer
     */
    override fun findStems(word: String, pos: POS?): List<String> {
        var word = normalize(word)

        // if pos is null, do all
        if (pos == null) {
            return POS.entries.asSequence()
                .flatMap { findStems(word, it) }
                .distinct()
                .toList()
        }

        val isMultipleWord = word.contains(UNDERSCORE)
        return when (pos) {
            POS.NOUN      -> if (isMultipleWord) getNounMultipleWordsRoots(word) else stripNounSuffix(word)
            POS.VERB      -> if (isMultipleWord) getVerbMultipleWordsRoots(word) else stripVerbSuffix(word)
            POS.ADJECTIVE -> stripAdjectiveSuffix(word)
            POS.ADVERB    -> emptyList()  // nothing for adverb
        }

        throw IllegalArgumentException("This should not happen")
    }

    /**
     * Converts all whitespace runs to single underscores. Tests first to see if there is any whitespace before converting.
     *
     * @param word the string to be normalized
     * @return a normalized string
     * @throws IllegalArgumentException if the specified string is empty or all whitespace
     */
    protected fun normalize(word: String): String {
        // make lowercase
        var word = word.lowercase()

        // replace all underscores with spaces
        word = word.replace('_', ' ')

        // trim off extra whitespace
        word = word.trim { it <= ' ' }
        require(word.isNotEmpty())

        // replace all whitespace with underscores
        return whitespace.matcher(word).replaceAll(UNDERSCORE)
    }

    /**
     * Strips suffixes from the specified word according to the noun rules.
     *
     * @param noun the word to be modified
     * @return a list of modified forms that were constructed, or the empty list if none
     */
    protected fun stripNounSuffix(noun: String): List<String> {
        if (noun.length <= 2) {
            return emptyList()
        }

        // strip off "ful" to later reapply it
        var word = noun
        var suffix: String? = null
        if (noun.endsWith(SUFFIX_ful)) {
            word = noun.substring(0, noun.length - SUFFIX_ful.length)
            suffix = SUFFIX_ful
        }

        return rules[POS.VERB]!!.asSequence()
            .mapNotNull { it.apply(word, suffix) }
            .filter { !it.isEmpty() }
            .distinct()
            .toList()
    }

    /**
     * Strips suffixes from the specified word according to the verb rules.
     *
     * @param verb the word to be modified
     * @return a list of modified forms that were constructed, or the empty list if none
     */

    protected fun stripVerbSuffix(verb: String): List<String> {
        if (verb.length <= 2) {
            return emptyList<String>()
        }
        return rules[POS.VERB]!!.asSequence()
            .mapNotNull { it.apply(verb) }
            .filter { !it.isEmpty() }
            .distinct()
            .toList()
    }

    /**
     * Strips suffixes from the specified word according to the adjective rules.
     *
     * @param adj the word to be modified
     * @return a list of modified forms that were constructed, or an empty list if none
     */
    protected fun stripAdjectiveSuffix(adj: String): List<String> {
        return rules[POS.ADJECTIVE]!!
            .asSequence()
            .mapNotNull { it.apply(adj) }
            .filter { !it.isEmpty() }
            .distinct()
            .toList()
    }

     /**
     * Handles stemming noun collocations.
     *
     * @param composite the word to be modified
     * @return a list of modified forms that were constructed, or the empty list if none
     */
    protected fun getNounMultipleWordsRoots(composite: String): List<String> {
        // split into parts
        val parts = composite.split(underscoreRegex).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size < 2) {
            return emptyList()
        }

        // stem each part
        val rootSets = parts
            .map { findStems(it, POS.NOUN) }
            .toList()

        // reassemble all combinations
        val product = cartesianProduct(rootSets)
        return product
            .map { it.joinToString(separator = UNDERSCORE) }
            .map { it.trim { it <= ' ' } }
            .filterNot { it.isEmpty() }
            .distinct()
            .toList()
    }

    /**
     * Handles stemming verb collocations.
     *
     * @param composite the word to be modified
     * @return a list of modified forms that were constructed, or an empty list if none
     */
    protected fun getVerbMultipleWordsRoots(composite: String): List<String> {
        // split into parts
        val parts = composite.split(underscoreRegex).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size < 2) {
            return emptyList()
        }

        // find the stems of each parts
        val rootSets = parts
            .map { findStems(it, POS.VERB) }
            .toList()

        // reassemble all combinations
        val product = cartesianProduct(rootSets)
        return product
            .map { it.joinToString(separator = UNDERSCORE) }
            .map { it.trim { it <= ' ' } }
            .filterNot { it.isEmpty() }
            .distinct()
            .toList()
    }

    companion object {

        val whitespace: Pattern = Pattern.compile("\\s+")

        const val UNDERSCORE = "_"

        val underscoreRegex: Regex = UNDERSCORE.toRegex()

        const val SUFFIX_ches: String = "ches"
        const val SUFFIX_ed: String = "ed"
        const val SUFFIX_es: String = "es"
        const val SUFFIX_est: String = "est"
        const val SUFFIX_er: String = "er"
        const val SUFFIX_ful: String = "ful"
        const val SUFFIX_ies: String = "ies"
        const val SUFFIX_ing: String = "ing"
        const val SUFFIX_men: String = "men"
        const val SUFFIX_s: String = "s"
        const val SUFFIX_ss: String = "ss"
        const val SUFFIX_ses: String = "ses"
        const val SUFFIX_shes: String = "shes"
        const val SUFFIX_xes: String = "xes"
        const val SUFFIX_zes: String = "zes"

        const val ENDING_null: String = ""
        const val ENDING_ch: String = "ch"
        const val ENDING_e: String = "e"
        const val ENDING_man: String = "man"
        const val ENDING_s: String = SUFFIX_s
        const val ENDING_sh: String = "sh"
        const val ENDING_x: String = "x"
        const val ENDING_y: String = "y"
        const val ENDING_z: String = "z"

        val rulesForNoun = listOf(
            StemmingRule(SUFFIX_s, ENDING_null, POS.NOUN, SUFFIX_ss),
            StemmingRule(SUFFIX_ses, ENDING_s, POS.NOUN),
            StemmingRule(SUFFIX_xes, ENDING_x, POS.NOUN),
            StemmingRule(SUFFIX_zes, ENDING_z, POS.NOUN),
            StemmingRule(SUFFIX_ches, ENDING_ch, POS.NOUN),
            StemmingRule(SUFFIX_shes, ENDING_sh, POS.NOUN),
            StemmingRule(SUFFIX_men, ENDING_man, POS.NOUN),
            StemmingRule(SUFFIX_ies, ENDING_y, POS.NOUN)
        )

        val rulesForVerb = listOf(
            StemmingRule(SUFFIX_s, ENDING_null, POS.VERB),
            StemmingRule(SUFFIX_ies, ENDING_y, POS.VERB),
            StemmingRule(SUFFIX_es, ENDING_e, POS.VERB),
            StemmingRule(SUFFIX_es, ENDING_null, POS.VERB),
            StemmingRule(SUFFIX_ed, ENDING_e, POS.VERB),
            StemmingRule(SUFFIX_ed, ENDING_null, POS.VERB),
            StemmingRule(SUFFIX_ing, ENDING_e, POS.VERB),
            StemmingRule(SUFFIX_ing, ENDING_null, POS.VERB)
        )

        val rulesForAdj = listOf(
            StemmingRule(SUFFIX_er, ENDING_e, POS.ADJECTIVE),
            StemmingRule(SUFFIX_er, ENDING_null, POS.ADJECTIVE),
            StemmingRule(SUFFIX_est, ENDING_e, POS.ADJECTIVE),
            StemmingRule(SUFFIX_est, ENDING_null, POS.ADJECTIVE)
        )

        val rulesForAdv = emptyList<StemmingRule>()

        val rules = sortedMapOf(
            POS.NOUN to rulesForNoun,
            POS.VERB to rulesForVerb,
            POS.ADJECTIVE to rulesForAdj,
            POS.ADVERB to rulesForAdv,
        )

        fun <T> cartesianProduct(lists: List<List<T>>): List<List<T>> {
            return lists.fold(listOf(emptyList<T>())) { acc, list ->
                acc.flatMap { accItem ->
                    list.map { accItem + it }
                }
            }
        }
    }
}
