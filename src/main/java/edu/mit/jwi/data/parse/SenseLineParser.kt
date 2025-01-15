package edu.mit.jwi.data.parse

import edu.mit.jwi.item.SenseEntry
import edu.mit.jwi.item.SenseKey
import java.util.*

/**
 * Parser for Wordnet sense index files (e.g., `index.sense` or `sense.index`).
 * It produces a SenseEntry object.
 *
 * @param keyParser the sense key parser this sense line parser should use
 */
object SenseLineParser : ILineParser<SenseEntry> {

    private val keyParser: ILineParser<SenseKey> = SenseKeyParser

    override fun parseLine(line: String): SenseEntry {

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

    @JvmStatic
    fun parseSenseEntry(tokenizer: StringTokenizer, senseKey: SenseKey): SenseEntry {
        // get offset
        val synsetOffset = tokenizer.nextToken().toInt()

        // get sense number
        val senseNumber = tokenizer.nextToken().toInt()

        // get tag cnt
        val tagCnt = tokenizer.nextToken().toInt()

        return SenseEntry(senseKey, synsetOffset, senseNumber, tagCnt)
    }
}
