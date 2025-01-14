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

import edu.mit.jwi.item.LexFile
import edu.mit.jwi.item.LexFile.Companion.getLexicalFile
import edu.mit.jwi.item.POS.Companion.getPartOfSpeech
import edu.mit.jwi.item.POS.Companion.isAdjectiveSatellite
import edu.mit.jwi.item.SenseKey

/**
 * A parser that takes a sense key string and produces an `SenseKey` object.
 */
object SenseKeyParser : ILineParser<SenseKey> {

    override fun parseLine(key: String): SenseKey {
        try {
            var begin = 0
            var end: Int = key.indexOf('%')

            // get lemma
            val lemma = key.substring(begin, end)

            // get ss_type
            begin = end + 1
            end = key.indexOf(':', begin)
            val ssType = key.substring(begin, end).toInt()
            val pos = getPartOfSpeech(ssType)!!
            val isAdjSat = isAdjectiveSatellite(ssType)

            // get lex_filenum
            begin = end + 1
            end = key.indexOf(':', begin)
            val lexFilenum = key.substring(begin, end).toInt()
            val lexFile = resolveLexicalFile(lexFilenum)

            // get lex_id
            begin = end + 1
            end = key.indexOf(':', begin)
            val lexId = key.substring(begin, end).toInt()

            // if it's not an adjective satellite, we're done
            if (!isAdjSat) {
                return SenseKey(lemma, lexId, pos!!, lexFile, null, -1, key)
            }

            // get head_word
            begin = end + 1
            end = key.indexOf(':', begin)
            val headWord = key.substring(begin, end)

            // get head_id
            begin = end + 1
            val headId = key.substring(begin).toInt()
            return SenseKey(lemma, lexId, pos, lexFile, headWord, headId, key)

        } catch (e: Exception) {
            throw MisformattedLineException(e)
        }
    }

    /**
     * Retrieves the lexical file objects for the [.parseLine] method.
     * If the lexical file number does correspond to a known lexical file, the method returns a singleton placeholder 'unknown' lexical file object.
     * This is implemented in its own method for ease of subclassing.
     *
     * @param lexFileNum the number of the lexical file to return
     * @return the lexical file corresponding to the specified frame number
     */
    private fun resolveLexicalFile(lexFileNum: Int): LexFile {
        return getLexicalFile(lexFileNum)
    }
}
