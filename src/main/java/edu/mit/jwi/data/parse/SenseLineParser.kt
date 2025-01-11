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

import edu.mit.jwi.item.ISenseEntry
import edu.mit.jwi.item.ISenseKey
import edu.mit.jwi.item.SenseEntry
import java.util.*

/**
 * Parser for Wordnet sense index files (e.g., `index.sense` or
 * `sense.index`). It produces an `ISenseEntry` object.
 *
 *
 * This class follows a singleton design pattern, and is not intended to be
 * instantiated directly; rather, call the [.getInstance] method to get
 * the singleton instance.
 *
 * This constructor is marked protected so that the class may be
 * sub-classed, but not directly instantiated. Obtain instances of this
 * class via the static [.getInstance] method.
 *
 * @param keyParser the sense key parser this sense line parser should use
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.1.0
 */
class SenseLineParser private constructor(private val keyParser: ILineParser<ISenseKey> = SenseKeyParser.instance!!) : ILineParser<ISenseEntry> {

    override fun parseLine(line: String): ISenseEntry {

        try {
            // get sense key
            val end = line.indexOf(' ')
            val keyStr = line.substring(0, end)
            checkNotNull(keyParser)
            val senseKey = keyParser.parseLine(keyStr)

            // get sense entry
            val tail = line.substring(end + 1)
            val tokenizer = StringTokenizer(tail)
            return parseSenseEntry(tokenizer, senseKey)
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
        var instance: SenseLineParser? = null
            get() {
                if (field == null) {
                    field = SenseLineParser()
                }
                return field
            }
            private set

        @JvmStatic
        fun parseSenseEntry(tokenizer: StringTokenizer, senseKey: ISenseKey): SenseEntry {
            // get offset
            val synsetOffset = tokenizer.nextToken().toInt()

            // get sense number
            val senseNumber = tokenizer.nextToken().toInt()

            // get tag cnt
            val tagCnt = tokenizer.nextToken().toInt()

            return SenseEntry(senseKey, synsetOffset, senseNumber, tagCnt)
        }
    }
}
