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

import edu.mit.jwi.NonNull
import edu.mit.jwi.Nullable
import edu.mit.jwi.item.POS
import java.util.*
import java.util.Collections.unmodifiableList
import java.util.Collections.unmodifiableMap
import java.util.regex.Pattern

/**
 * Provides simple a simple pattern-based stemming facility based on the "Rules
 * of Detachment" as described in the `morphy` man page in the Wordnet
 * distribution, which can be found at [
 * http://wordnet.princeton.edu/man/morphy.7WN.html](http://wordnet.princeton.edu/man/morphy.7WN.html) It also attempts to
 * strip "ful" endings. It does not search Wordnet to see if stems actually
 * exist. In particular, quoting from that man page:
 * <h3>Rules of Detachment</h3>
 *
 *
 * The following table shows the rules of detachment used by Morphy. If a word
 * ends with one of the suffixes, it is stripped from the word and the
 * corresponding ending is added. ... No rules are applicable to adverbs.
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
 *
 * Morphy contains code that searches for nouns ending with ful and performs a
 * transformation on the substring preceding it. It then appends 'ful' back
 * onto the resulting string and returns it. For example, if passed the nouns
 * "boxesful", it will return "boxful".
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
open class SimpleStemmer : IStemmer {

    val whitespace: Pattern = Pattern.compile("\\s+")

    /**
     * Returns a set of stemming rules used by this stemmer. Will not return a
     * null map, but it may be empty. The lists in the map will also not be
     * null, but may be empty.
     *
     * @return the rule map for this stemmer
     * @since JWI 3.5.1
     */
    val ruleMap: MutableMap<POS?, List<StemmingRule>>
        get() = Companion.ruleMap

    override fun findStems(word: String, @Nullable pos: POS?): List<String> {
        var word = word
        word = normalize(word)

        // if pos is null, do all
        if (pos == null) {
            val result: MutableSet<String?> = LinkedHashSet<String?>()
            for (p in POS.entries) {
                result.addAll(findStems(word, p))
            }
            if (result.isEmpty()) {
                return listOf<String>()
            }
            return ArrayList<String>(result)
        }

        val isCollocation: Boolean = word.contains(underscore)

        return when (pos) {
            POS.NOUN      -> if (isCollocation) getNounCollocationRoots(word) else stripNounSuffix(word)
            POS.VERB      -> if (isCollocation) getVerbCollocationRoots(word) else stripVerbSuffix(word) // BUG006: here we check for composites

            POS.ADJECTIVE -> stripAdjectiveSuffix(word)
            POS.ADVERB    -> listOf<String>()  // nothing for adverb
        }

        throw IllegalArgumentException("This should not happen")
    }

    /**
     * Converts all whitespace runs to single underscores. Tests first to see if
     * there is any whitespace before converting.
     *
     * @param word the string to be normalized
     * @return a normalized string
     * @throws NullPointerException     if the specified string is `null`
     * @throws IllegalArgumentException if the specified string is empty or all whitespace
     * @since JWI 2.1.1
     */
    protected fun normalize(word: String): String {
        // make lowercase
        var word = word
        word = word.lowercase(Locale.getDefault())

        // replace all underscores with spaces
        word = word.replace('_', ' ')

        // trim off extra whitespace
        word = word.trim { it <= ' ' }
        require(word.isNotEmpty())

        // replace all whitespace with underscores
        word = whitespace.matcher(word).replaceAll(underscore)

        // return normalized word
        return word
    }

    /**
     * Strips suffixes from the specified word according to the noun rules.
     *
     * @param noun the word to be modified
     * @return a list of modified forms that were constructed, or the empty list
     * if none
     * @throws NullPointerException if the specified word is `null`
     * @since JWI 1.0
     */
    @NonNull
    protected fun stripNounSuffix(noun: String): List<String> {
        if (noun.length <= 2) {
            return listOf<String>()
        }

        // strip off "ful"
        var word = noun
        var suffix: String? = null
        if (noun.endsWith(SUFFIX_ful)) {
            word = noun.substring(0, noun.length - SUFFIX_ful.length)
            suffix = SUFFIX_ful
        }

        // we will return this to the caller
        val result: MutableSet<String> = LinkedHashSet<String>()

        // apply the rules
        var root: String?
        val rules = checkNotNull(this.ruleMap[POS.NOUN])
        for (rule in rules) {
            root = rule.apply(word, suffix)
            if (root != null && root.isNotEmpty()) {
                result.add(root)
            }
        }
        return if (result.isEmpty()) listOf<String>() else ArrayList<String>(result)
    }

    /**
     * Handles stemming noun collocations.
     *
     * @param composite the word to be modified
     * @return a list of modified forms that were constructed, or the empty list
     * if none
     * @throws NullPointerException if the specified word is `null`
     * @since JWI 1.1.1
     */
    @NonNull
    protected fun getNounCollocationRoots(composite: String): List<String> {
        // split into parts
        val parts: Array<String> = composite.split(underscore.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size < 2) {
            return mutableListOf<String>()
        }

        // stem each part
        val rootSets: MutableList<List<String>?> = ArrayList<List<String>?>(parts.size)
        for (part in parts) {
            rootSets.add(findStems(part, POS.NOUN))
        }

        // reassemble all combinations
        val poss: MutableSet<StringBuffer> = HashSet<StringBuffer>()

        // seed the set
        var rootSet = rootSets[0]
        if (rootSet == null) {
            poss.add(StringBuffer(parts[0]))
        } else {
            for (root in rootSet) {
                poss.add(StringBuffer(root))
            }
        }

        // make all combinations
        var replace: MutableSet<StringBuffer>?
        for (i in 1..<rootSets.size) {
            rootSet = rootSets[i]
            if (rootSet!!.isEmpty()) {
                for (p in poss) {
                    p.append("_")
                    p.append(parts[i])
                }
            } else {
                replace = HashSet<StringBuffer>()
                for (p in poss) {
                    for (root in rootSet) {
                        var newBuf = StringBuffer()
                        newBuf.append(p.toString())
                        newBuf.append("_")
                        newBuf.append(root)
                        replace.add(newBuf)
                    }
                }
                poss.clear()
                poss.addAll(replace)
            }
        }

        if (poss.isEmpty()) {
            return listOf<String>()
        }

        // make sure to remove empties
        val result: MutableSet<String> = LinkedHashSet<String>()
        var root: String?
        for (p in poss) {
            root = p.toString().trim { it <= ' ' }
            if (root.isNotEmpty()) {
                result.add(root)
            }
        }
        return ArrayList<String>(result)
    }

    /**
     * Strips suffixes from the specified word according to the verb rules.
     *
     * @param verb the word to be modified
     * @return a list of modified forms that were constructed, or the empty list
     * if none
     * @throws NullPointerException if the specified word is `null`
     * @since JWI 1.0
     */
    @NonNull
    protected fun stripVerbSuffix(@NonNull verb: String): List<String> {
        if (verb.length <= 2) {
            return emptyList<String>()
        }

        // we will return this to the caller
        val result: MutableSet<String?> = LinkedHashSet<String?>()

        // apply the rules
        var root: String?
        val rules = checkNotNull(this.ruleMap[POS.VERB])
        for (rule in rules) {
            root = rule.apply(verb)
            if (root != null && root.isNotEmpty()) {
                result.add(root)
            }
        }
        return if (result.isEmpty()) emptyList<String>() else ArrayList<String>(result)
    }

    /**
     * Handles stemming verb collocations.
     *
     * @param composite the word to be modified
     * @return a list of modified forms that were constructed, or an empty list
     * if none
     * @throws NullPointerException if the specified word is `null`
     * @since JWI 1.1.1
     */
    protected fun getVerbCollocationRoots(composite: String): List<String> {
        // split into parts
        val parts: Array<String> = composite.split(underscore.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size < 2) {
            return listOf<String>()
        }

        // find the stems of each parts
        val rootSets: MutableList<List<String>> = ArrayList<List<String>>(parts.size)
        for (part in parts) {
            rootSets.add(findStems(part, POS.VERB))
        }

        val result: MutableSet<String?> = LinkedHashSet<String?>()

        // form all combinations
        val rootBuffer = StringBuilder()
        for (i in parts.indices) {
            if (rootSets[i] == null) {
                continue
            }
            for (partRoot in rootSets[i]) {
                rootBuffer.replace(0, rootBuffer.length, "")

                for (j in parts.indices) {
                    if (j == i) {
                        rootBuffer.append(partRoot)
                    } else {
                        rootBuffer.append(parts[j])
                    }
                    if (j < parts.size - 1) {
                        rootBuffer.append(underscore)
                    }
                }
                result.add(rootBuffer.toString())
            }
        }

        // remove any empties
        result.removeIf { s: String? -> s!!.isEmpty() }
        return if (result.isEmpty()) listOf<String>() else ArrayList<String>(result)
    }

    /**
     * Strips suffixes from the specified word according to the adjective rules.
     *
     * @param adj the word to be modified
     * @return a list of modified forms that were constructed, or an empty list
     * if none
     * @throws NullPointerException if the specified word is `null`
     * @since JWI 1.0
     */
    protected fun stripAdjectiveSuffix(adj: String): List<String> {
        // we will return this to the caller
        val result: MutableSet<String> = LinkedHashSet<String>()

        // apply the rules
        var root: String?
        val rules = checkNotNull(this.ruleMap[POS.ADJECTIVE])
        for (rule in rules) {
            root = rule.apply(adj)
            if (root != null && root.isNotEmpty()) {
                result.add(root)
            }
        }
        return if (result.isEmpty()) listOf<String>() else ArrayList<String>(result)
    }

    companion object {

        const val underscore: String = "_"
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

        val ruleMap: MutableMap<POS?, List<StemmingRule>>

        init {
            // nouns
            val listNouns = listOf(
                StemmingRule(SUFFIX_s, ENDING_null, POS.NOUN, SUFFIX_ss),
                StemmingRule(SUFFIX_ses, ENDING_s, POS.NOUN),
                StemmingRule(SUFFIX_xes, ENDING_x, POS.NOUN),
                StemmingRule(SUFFIX_zes, ENDING_z, POS.NOUN),
                StemmingRule(SUFFIX_ches, ENDING_ch, POS.NOUN),
                StemmingRule(SUFFIX_shes, ENDING_sh, POS.NOUN),
                StemmingRule(SUFFIX_men, ENDING_man, POS.NOUN),
                StemmingRule(SUFFIX_ies, ENDING_y, POS.NOUN)
            )

            // verbs
            val listVerb = listOf(
                StemmingRule(SUFFIX_s, ENDING_null, POS.VERB),
                StemmingRule(SUFFIX_ies, ENDING_y, POS.VERB),
                StemmingRule(SUFFIX_es, ENDING_e, POS.VERB),
                StemmingRule(SUFFIX_es, ENDING_null, POS.VERB),
                StemmingRule(SUFFIX_ed, ENDING_e, POS.VERB),
                StemmingRule(SUFFIX_ed, ENDING_null, POS.VERB),
                StemmingRule(SUFFIX_ing, ENDING_e, POS.VERB),
                StemmingRule(SUFFIX_ing, ENDING_null, POS.VERB)
            )

            // adjectives
            val listAdj = listOf(
                StemmingRule(SUFFIX_er, ENDING_e, POS.ADJECTIVE),
                StemmingRule(SUFFIX_er, ENDING_null, POS.ADJECTIVE),
                StemmingRule(SUFFIX_est, ENDING_e, POS.ADJECTIVE),
                StemmingRule(SUFFIX_est, ENDING_null, POS.ADJECTIVE)
            )

            // adverbs
            val listAdv = emptyList<StemmingRule>()

            // assign
            val ruleMapHidden = sortedMapOf(
                POS.NOUN to unmodifiableList<StemmingRule>(listNouns),
                POS.VERB to unmodifiableList<StemmingRule>(listVerb),
                POS.ADJECTIVE to unmodifiableList<StemmingRule>(listAdj),
                POS.ADVERB to unmodifiableList<StemmingRule>(listAdv),
            )
            ruleMap = unmodifiableMap<POS?, List<StemmingRule>>(ruleMapHidden)
        }
    }
}
