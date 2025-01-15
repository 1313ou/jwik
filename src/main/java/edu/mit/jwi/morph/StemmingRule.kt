package edu.mit.jwi.morph

import edu.mit.jwi.item.POS
import java.util.*

/**
 * Default implementation of the [IStemmingRule] interface.
 *
 * Creates a new stemming rule with the specified suffix, ending, and
 * avoid set
 *
 * @param suffix the suffix that should be stripped from a word; should not be empty, or all whitespace.
 * @param ending the ending that should be stripped from a word; may be empty or all whitespace.
 * @param pos    the part of speech to which this rule applies
 * @param ignore the set of suffixes that, when present, indicate this rule should not be applied. May not contain nulls or empties.
 */
class StemmingRule(suffix: String, ending: String, pos: POS, vararg ignore: String) : IStemmingRule {

    override val pOS: POS

    override val suffix: String

    override val ending: String

    override val suffixIgnoreSet: MutableSet<String>

     init {
        var suffix = suffix
        var ending = ending

        // allocate avoid set
        var ignoreSet: MutableSet<String>
        if (ignore.isNotEmpty()) {
            ignoreSet = HashSet<String>(ignore.size)
            for (avoidStr in ignore) {
                val avoidStr = avoidStr.trim { it <= ' ' }
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
        sb.append(suffix.trim { it <= ' ' })
        return sb.toString()
    }
}
