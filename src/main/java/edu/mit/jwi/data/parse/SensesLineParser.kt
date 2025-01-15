package edu.mit.jwi.data.parse

import edu.mit.jwi.data.parse.SenseLineParser.parseSenseEntry
import edu.mit.jwi.item.SenseEntry
import edu.mit.jwi.item.SenseKey
import java.util.*

/**
 * Parser for Wordnet sense index files (e.g., `index.sense` or`sense.index`).
 * It produces an array of SenseEntry object.
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
