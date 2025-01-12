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

import edu.mit.jwi.item.*
import edu.mit.jwi.item.POS.Companion.getPartOfSpeech
import edu.mit.jwi.item.Pointer.Companion.getPointerType
import java.util.*

/**
 * Parser for Wordnet index files (e.g., `idx.adv` or
 * `adv.idx`). It produces an `IndexWord` object.
 *
 * This class follows a singleton design pattern, and is not intended to be
 * instantiated directly; rather, call the [.getInstance] method to get
 * the singleton instance.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
class IndexLineParser
/**
 * This constructor is marked protected so that the class may be
 * sub-classed, but not directly instantiated. Obtain instances of this
 * class via the static [.getInstance] method.
 *
 * @since JWI 2.0.0
 */
private constructor() : ILineParser<IndexWord> {

    override fun parseLine(line: String): IndexWord {

        try {
            val tokenizer = StringTokenizer(line, " ")

            // get lemma
            val lemma = tokenizer.nextToken()

            // get pos
            val posSym = tokenizer.nextToken()
            val pos = getPartOfSpeech(posSym[0])

            // consume synset_cnt
            tokenizer.nextToken()

            // consume ptr_symbols
            val ptrCnt = tokenizer.nextToken().toInt()
            val ptrs = Array<Pointer>(ptrCnt) {
                val tok: String = tokenizer.nextToken()
                resolvePointer(tok, pos)
            }

            // get sense_cnt
            val senseCount = tokenizer.nextToken().toInt()

            // get tagged sense count
            val tagSenseCnt = tokenizer.nextToken().toInt()

            // get words
            val words = Array<IWordID>(senseCount) {
                val offset: Int = tokenizer.nextToken().toInt()
                WordLemmaID(SynsetID(offset, pos), lemma)
            }
            return IndexWord(lemma, pos, tagSenseCnt, ptrs, words)
        } catch (e: Exception) {
            throw MisformattedLineException(line, e)
        }
    }

    /**
     * Retrieves the pointer objects for the [.parseLine] method.
     *
     * This is implemented in its own method for ease of subclassing.
     *
     * @param symbol the symbol of the pointer to return
     * @param pos    the part of speech of the pointer to return, can be
     * null unless the pointer symbol is ambiguous
     * @return the pointer corresponding to the specified symbol and part of
     * speech combination
     * @throws NullPointerException     if the symbol is null
     * @throws IllegalArgumentException if the symbol and part of speech combination does not
     * correspond to a known pointer
     * @since JWI 2.3.0
     */
    private fun resolvePointer(symbol: String, pos: POS?): Pointer {
        return getPointerType(symbol, pos)
    }

    companion object {

        /**
         * Returns the singleton instance of this class, instantiating it if
         * necessary. The singleton instance will not be null.
         *
         * @return the non-null singleton instance of this class,
         * instantiating it if necessary.
         * @since JWI 2.0.0
         */
        var instance: IndexLineParser? = null
            get() {
                if (field == null) {
                    field = IndexLineParser()
                }
                return field
            }
            private set
    }
}
