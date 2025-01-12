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
 * A parser that takes a sense key string and produces an `SenseKey`
 * object.
 *
 * This class follows a singleton design pattern, and is not intended to be
 * instantiated directly; rather, call the [.getInstance] method to get
 * the singleton instance.
 *
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.1.4
 */
class SenseKeyParser
/**
 * This constructor is marked protected so that the class may be
 * sub-classed, but not directly instantiated. Obtain instances of this
 * class via the static [.getInstance] method.
 *
 * @since JWI 2.1.4
 */
private constructor() : ILineParser<SenseKey> {

    override fun parseLine(key: String): SenseKey {
        try {
            var begin = 0

            // get lemma
            var end: Int = key.indexOf('%')
            val lemma = key.substring(begin, end)

            // get ss_type
            begin = end + 1
            end = key.indexOf(':', begin)
            val ss_type = key.substring(begin, end).toInt()
            val pos = getPartOfSpeech(ss_type)
            val isAdjSat = isAdjectiveSatellite(ss_type)

            // get lex_filenum
            begin = end + 1
            end = key.indexOf(':', begin)
            val lex_filenum = key.substring(begin, end).toInt()
            val lexFile = resolveLexicalFile(lex_filenum)

            // get lex_id
            begin = end + 1
            end = key.indexOf(':', begin)
            val lex_id = key.substring(begin, end).toInt()

            // if it's not an adjective satellite, we're done
            if (!isAdjSat) {
                return SenseKey(lemma, lex_id, pos!!, lexFile, null, -1, key)
            }

            // get head_word
            begin = end + 1
            end = key.indexOf(':', begin)
            val head_word = key.substring(begin, end)

            // get head_id
            begin = end + 1
            val head_id = key.substring(begin).toInt()
            return SenseKey(lemma, lex_id, pos!!, lexFile, head_word, head_id, key)
        } catch (e: Exception) {
            throw MisformattedLineException(e)
        }
    }

    /**
     *
     *
     * Retrieves the lexical file objects for the [.parseLine]
     * method. If the lexical file number does correspond to a known lexical
     * file, the method returns a singleton placeholder 'unknown' lexical file
     * object.
     *
     *
     *
     * This is implemented in its own method for ease of subclassing.
     *
     *
     * @param lexFileNum the number of the lexical file to return
     * @return the lexical file corresponding to the specified frame number
     * @since JWI 2.1.0
     */
    private fun resolveLexicalFile(lexFileNum: Int): LexFile {
        return getLexicalFile(lexFileNum)
    }

    companion object {

        /**
         * Returns the singleton instance of this class, instantiating it if
         * necessary. The singleton instance will not be null.
         *
         * @return the non-null singleton instance of this class,
         * instantiating it if necessary.
         * @since JWI 2.1.4
         */
        @JvmStatic
        var instance: SenseKeyParser? = null
            get() {
                if (field == null) {
                    field = SenseKeyParser()
                }
                return field
            }
            private set
    }
}
