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
 * Parser for Wordnet sense index files (e.g., `index.sense` or`sense.index`). It produces an `SenseEntry` object.
 */
object SensesLineParser : ILineParser<Array<SenseEntry>> {

    private val keyParser: ILineParser<SenseKey> = SenseKeyParser

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
}
