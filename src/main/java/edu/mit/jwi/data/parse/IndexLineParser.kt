package edu.mit.jwi.data.parse

import edu.mit.jwi.item.*
import edu.mit.jwi.item.POS.Companion.getPartOfSpeech
import edu.mit.jwi.item.Pointer.Companion.getPointerType
import java.util.*

/**
 * Parser for Wordnet index files (e.g., `idx.adv` or `adv.idx`).
 * It produces an IndexWord object.
 */
object IndexLineParser : ILineParser<SenseIndex> {

    override fun parseLine(line: String): SenseIndex {

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
            val words = Array<ISenseID>(senseCount) {
                val offset: Int = tokenizer.nextToken().toInt()
                SenseIDWithLemma(SynsetID(offset, pos), lemma)
            }
            return SenseIndex(lemma, pos, tagSenseCnt, ptrs, words)
        } catch (e: Exception) {
            throw MisformattedLineException(line, e)
        }
    }

    /**
     * Retrieves the pointer objects for the parseLine method.
     * This is implemented in its own method for ease of subclassing.
     *
     * @param symbol the symbol of the pointer to return
     * @param pos    the part of speech of the pointer to return, can be null unless the pointer symbol is ambiguous
     * @return the pointer corresponding to the specified symbol and part of speech combination
     * @throws IllegalArgumentException if the symbol and part of speech combination does not correspond to a known pointer
     */
    private fun resolvePointer(symbol: String, pos: POS?): Pointer {
        return getPointerType(symbol, pos)
    }
}
