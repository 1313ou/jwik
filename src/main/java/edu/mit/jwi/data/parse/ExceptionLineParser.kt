package edu.mit.jwi.data.parse

import edu.mit.jwi.item.ExceptionEntryProxy
import java.util.regex.Pattern

/**
 * Parser for Wordnet exception files (e.g., `exc.adv` or `adv.exc`).
 * This parser produces ExceptionEntryProxy objects instead of IExceptionEntry objects directly because the exception files do not contain information about part of speech.
 * This needs to be added by the governing object to create a full-fledged IExceptionEntry object.
 */
object ExceptionLineParser : ILineParser<ExceptionEntryProxy> {

    override fun parseLine(line: String): ExceptionEntryProxy {

        val forms: Array<String> = spacePattern.split(line)
        if (forms.size < 2) {
            throw MisformattedLineException(line)
        }
        val surface = forms[0].trim { it <= ' ' }
        val trimmed = Array<String>(forms.size - 1) {
            forms[it + 1].trim { it <= ' ' }
        }
        return ExceptionEntryProxy(surface, trimmed)
    }

    private val spacePattern: Pattern = Pattern.compile(" ")
}
