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

import edu.mit.jwi.data.parse.SenseLineParser.parseSenseEntry
import edu.mit.jwi.item.SenseEntry
import edu.mit.jwi.item.SenseKey
import java.util.*

/**
 * Parser for Wordnet sense index files (e.g., `index.sense` or
 * `sense.index`). It produces an `SenseEntry` object.
 *
 * This class follows a singleton design pattern, and is not intended to be
 * instantiated directly; rather, call the [.getInstance] method to get
 * the singleton instance.
 *
 * This constructor is marked protected so that the class may be
 * sub-classed, but not directly instantiated. Obtain instances of this
 * class via the static [.getInstance] method.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.1.0
 */
class SensesLineParser private constructor(private val keyParser: ILineParser<SenseKey> = SenseKeyParser) : ILineParser<Array<SenseEntry>> {

    override fun parseLine(line: String): Array<SenseEntry> {

        val senseEntries: MutableList<SenseEntry> = ArrayList<SenseEntry>()
        try {
            // get sense key
            val end = line.indexOf(' ')
            val keyStr = line.substring(0, end)
            checkNotNull(keyParser)
            val senseKey = keyParser.parseLine(keyStr)

            // get sense entry
            val tail = line.substring(end + 1)
            val tokenizer = StringTokenizer(tail)

            while (tokenizer.hasMoreTokens()) {
                senseEntries.add(parseSenseEntry(tokenizer, senseKey))
            }
            return senseEntries.toTypedArray<SenseEntry>()
        } catch (e: Exception) {
            throw MisformattedLineException(line, e)
        }
    }

    companion object {

        /**
         * Returns the singleton instance of this class, instantiating it if
         * necessary. The singleton instance will not be null.
         *
         * @return the non-null singleton instance of this class,
         * instantiating it if necessary.
         * @since JWI 2.1.0
         */
        var instance: SensesLineParser? = null
            get() {
                if (field == null) {
                    field = SensesLineParser()
                }
                return field
            }
            private set
    }
}
