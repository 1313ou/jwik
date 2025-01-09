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

import edu.mit.jwi.item.POS
import java.util.*

/**
 * Default implementation of the [IStemmingRule] interface.
 *
 * Creates a new stemming rule with the specified suffix, ending, and
 * avoid set
 *
 * @param suffix the suffix that should be stripped from a word; should not
 * be `null`, empty, or all whitespace.
 * @param ending the ending that should be stripped from a word; should not
 * be `null`, but may be empty or all whitespace.
 * @param pos    the part of speech to which this rule applies, may not be
 * `null`
 * @param ignore the set of suffixes that, when present, indicate this rule
 * should not be applied. May be null or empty, but not
 * contain nulls or empties.
 * @throws NullPointerException if the suffix, ending, or pos are null, or the ignore set
 * contains null
 * @throws NullPointerException if the suffix is empty or all whitespace, or the ignore
 * set contains a string which is empty or all whitespace

 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.3.1
 */
class StemmingRule(suffix: String, ending: String, pos: POS, vararg ignore: String) : IStemmingRule {

    override val pOS: POS

    override val suffix: String

    override val ending: String

    override val suffixIgnoreSet: MutableSet<String>

    /**
      * @since JWI 2.3.1
     */
    init {
        var suffix = suffix
        var ending = ending
        if (suffix == null) {
            throw NullPointerException()
        }
        if (ending == null) {
            throw NullPointerException()
        }
        if (pos == null) {
            throw NullPointerException()
        }

        // allocate avoid set
        var ignoreSet: MutableSet<String>
        if (ignore != null && ignore.isNotEmpty()) {
            ignoreSet = HashSet<String>(ignore.size)
            for (avoidStr in ignore) {
                var avoidStr = avoidStr
                if (avoidStr == null) {
                    throw NullPointerException()
                }
                avoidStr = avoidStr.trim { it <= ' ' }
                require(avoidStr.isNotEmpty())
                ignoreSet.add(avoidStr)
            }
            ignoreSet = Collections.unmodifiableSet<String>(ignoreSet)
        } else {
            ignoreSet = mutableSetOf<String>()
        }

        suffix = suffix.trim { it <= ' ' }
        ending = ending.trim { it <= ' ' }
        require(suffix.isNotEmpty())

        require(!ignoreSet.contains(suffix))

        this.pOS = pos
        this.suffix = suffix
        this.ending = ending
        this.suffixIgnoreSet = ignoreSet
    }

    override fun apply(word: String): String? {
        return apply(word, null)
    }

    override fun apply(word: String, suffix: String?): String? {
        // see if the suffix is present
        checkNotNull(suffix)
        if (!word.endsWith(suffix)) {
            return null
        }

        // process ignore set
        for (ignoreSuffix in suffixIgnoreSet) {
            if (word.endsWith(ignoreSuffix)) {
                return null
            }
        }

        // apply the rule
        // we loop directly over characters here to avoid two loops
        val sb = StringBuilder()
        val len = word.length - suffix.length
        for (i in 0..<len) {
            sb.append(word[i])
        }
        sb.append(ending)

        // append optional suffix
        if (suffix != null) {
            sb.append(suffix.trim { it <= ' ' })
        }
        return sb.toString()
    }
}