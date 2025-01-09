/* ******************************************************************************
 * Java Wordnet Interface Library (JWI) v2.4.0
 * Copyright (c) 2007-2015 Mark A. Finlayson
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 4.0
 * International Public License, which means it may be freely used for all
 * purposes, as long as proper acknowledgment is made.  See the license file
 * included with this distribution for more details.
 *******************************************************************************/
package edu.mit.jwi.data.parse

import edu.mit.jwi.item.ExceptionEntryProxy
import edu.mit.jwi.item.IExceptionEntryProxy
import java.util.regex.Pattern

/**
 *
 *
 * Parser for Wordnet exception files (e.g., `exc.adv` or
 * `adv.exc`). This parser produces `IExceptionEntryProxy`
 * objects instead of `IExceptionEntry` objects directly because the
 * exception files do not contain information about part of speech. This needs
 * to be added by the governing object to create a full-fledged
 * `IExceptionEntry` object.
 *
 *
 *
 * This class follows a singleton design pattern, and is not intended to be
 * instantiated directly; rather, call the [.getInstance] method to get
 * the singleton instance.
 *
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
class ExceptionLineParser
/**
 * This constructor is marked protected so that the class may be
 * sub-classed, but not directly instantiated. Obtain instances of this
 * class via the static [.getInstance] method.
 *
 * @since JWI 2.0.0
 */
private constructor() : ILineParser<IExceptionEntryProxy> {

    override fun parseLine(line: String): IExceptionEntryProxy {

        val forms: Array<String?> = spacePattern.split(line)
        if (forms.size < 2) {
            throw MisformattedLineException(line)
        }

        val surface = forms[0]!!.trim { it <= ' ' }

        val trimmed = Array<String>(forms.size - 1) {
            forms[it + 1]!!.trim { it <= ' ' }
        }
        return ExceptionEntryProxy(surface, trimmed)
    }

    companion object {

        /**
         * Returns the singleton instance of this class, instantiating it if
         * necessary. The singleton instance will not be `null`.
         *
         * @return the non-`null` singleton instance of this class,
         * instantiating it if necessary.
         * @since JWI 2.0.0
         */
        var instance: ExceptionLineParser? = null
            get() {
                if (field == null) {
                    field = ExceptionLineParser()
                }
                return field
            }
            private set

        // static fields
        private val spacePattern: Pattern = Pattern.compile(" ")
    }
}
