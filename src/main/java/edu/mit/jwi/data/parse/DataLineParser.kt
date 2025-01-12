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
import edu.mit.jwi.item.LexFile.Companion.getLexicalFile
import edu.mit.jwi.item.POS.Companion.getPartOfSpeech
import edu.mit.jwi.item.Pointer.Companion.getPointerType
import edu.mit.jwi.item.Synset.WordBuilder
import edu.mit.jwi.item.UnknownLexFile.Companion.getUnknownLexicalFile
import edu.mit.jwi.item.VerbFrame.Companion.getFrame
import java.util.*

/**
 *
 *
 * Parser for Wordnet data files (e.g., `data.adv` or
 * `adv.dat`). This parser produces an `Synset` object.
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
class DataLineParser
/**
 * This constructor is marked protected so that the class may be
 * sub-classed, but not directly instantiated. Obtain instances of this
 * class via the static [.getInstance] method.
 *
 * @since JWI 2.0.0
 */
private constructor() : ILineParser<Synset> {

    override fun parseLine(line: String): Synset {

        try {
            val tokenizer = StringTokenizer(line, " ")

            // Get offset
            val offset = tokenizer.nextToken().toInt()

            // Consume lex_filenum
            val lex_filenum = tokenizer.nextToken().toInt()
            val lexFile = resolveLexicalFile(lex_filenum)

            // Get part of speech
            val synset_pos: POS?
            val synset_tag = tokenizer.nextToken()[0]
            synset_pos = getPartOfSpeech(synset_tag)

            val synsetID = SynsetID(offset, synset_pos)

            // Determine if it is an adjective satellite
            val isAdjSat = (synset_tag == 's')

            // A synset is an adjective head if it is the 00 lexical file, is
            // not an adjective satellite, and it has an antonym. The Wordnet
            // definition says head synsets have to have an antonym, but this is
            // actually violated (perhaps mistakenly) in a small number of
            // cases, e.g., in Wordnet 3.0:
            // 01380267 aerial (no antonyms), with satellite 01380571 free-flying
            // 01380721 marine (no antonyms), with satellite 01380926 deep-sea
            val isAdjHead = !isAdjSat && lex_filenum == 0

            // Get word count
            val wordCount = tokenizer.nextToken().toInt(16)

            // Get words
            var lemma: String
            var marker: AdjMarker?
            var lexID: Int
            val wordProxies: Array<WordBuilder> = Array<WordBuilder>(wordCount) {
                // consume next word
                lemma = tokenizer.nextToken()

                // if it is an adjective, it may be followed by a marker
                marker = null
                if (synset_pos == POS.ADJECTIVE) {
                    for (adjMarker in AdjMarker.entries) {
                        checkNotNull(adjMarker.symbol)
                        if (lemma.endsWith(adjMarker.symbol)) {
                            marker = adjMarker
                            lemma = lemma.substring(0, lemma.length - adjMarker.symbol.length)
                        }
                    }
                }

                // parse lex_id
                lexID = tokenizer.nextToken().toInt(16)

                WordBuilder(it + 1, lemma, lexID, marker)
            }

            // Get pointers
            var synsetPointerMap: MutableMap<Pointer, ArrayList<SynsetID>>? = null
            val pointerCount = tokenizer.nextToken().toInt()
            repeat(pointerCount) {

                // get pointer symbol
                val pointer_type: Pointer = resolvePointer(tokenizer.nextToken(), synset_pos)
                checkNotNull(pointer_type)

                // get synset target offset
                val target_offset: Int = tokenizer.nextToken().toInt()

                // get target synset part of speech
                val target_pos = getPartOfSpeech(tokenizer.nextToken()[0])

                var target_synset_id = SynsetID(target_offset, target_pos)

                // get source/target numbers
                var source_target_num: Int = tokenizer.nextToken().toInt(16)

                // this is a semantic pointer if the source/target numbers are zero
                if (source_target_num == 0) {
                    if (synsetPointerMap == null) {
                        synsetPointerMap = HashMap<Pointer, ArrayList<SynsetID>>()
                    }
                    var pointerList: ArrayList<SynsetID> = synsetPointerMap.computeIfAbsent(pointer_type) { k: Pointer -> ArrayList<SynsetID>() }
                    pointerList.add(target_synset_id)
                } else {
                    // this is a lexical pointer
                    val source_num: Int = source_target_num / 256
                    val target_num: Int = source_target_num and 255
                    val target_word_id: IWordID = WordNumID(target_synset_id, target_num)
                    wordProxies[source_num - 1].addRelatedWord(pointer_type, target_word_id)
                }
            }

            // trim pointer lists
            if (synsetPointerMap != null) {
                for (list in synsetPointerMap.values) {
                    list.trimToSize()
                }
            }

            // parse verb frames
            // do not make the field compulsory for verbs with a 00 when no frame is present
            if (synset_pos == POS.VERB) {
                val peekTok = tokenizer.nextToken()
                if (!peekTok.startsWith("|")) {
                    val verbFrameCount = peekTok.toInt()
                    var frame: IVerbFrame
                    repeat(verbFrameCount) {
                        // Consume '+'
                        tokenizer.nextToken()
                        // Get frame number
                        var frame_num: Int = tokenizer.nextToken().toInt()
                        frame = resolveVerbFrame(frame_num)
                        // Get word number
                        val word_num: Int = tokenizer.nextToken().toInt(16)
                        if (word_num > 0) {
                            wordProxies[word_num - 1].addVerbFrame(frame)
                        } else {
                            for (proxy in wordProxies) {
                                proxy.addVerbFrame(frame)
                            }
                        }
                    }
                }
            }

            // Get gloss
            var gloss = ""
            val index = line.indexOf('|')
            if (index > 0) {
                gloss = line.substring(index + 2).trim { it <= ' ' }
            }

            // create synset and words
            val words = listOf<WordBuilder>(*wordProxies)
            return Synset(synsetID, lexFile, isAdjSat, isAdjHead, gloss, words, synsetPointerMap)
        } catch (e: NumberFormatException) {
            throw MisformattedLineException(line, e)
        } catch (e: NoSuchElementException) {
            throw MisformattedLineException(line, e)
        }
    }

    /**
     *
     *
     * Retrieves the verb frames for the [.parseLine] method.
     *
     *
     *
     * This is implemented in its own method for ease of subclassing.
     *
     *
     * @param frameNum the number of the frame to return
     * @return the verb frame corresponding to the specified frame number, or
     * null if there is none
     * @since JWI 2.1.0
     */
    private fun resolveVerbFrame(frameNum: Int): IVerbFrame {
        return getFrame(frameNum)!!
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
    private fun resolveLexicalFile(lexFileNum: Int): ILexFile {
        var lexFile: ILexFile = getLexicalFile(lexFileNum)
        if (lexFile == null) {
            lexFile = getUnknownLexicalFile(lexFileNum)
        }
        return lexFile
    }

    /**
     *
     *
     * Retrieves the pointer objects for the [.parseLine] method.
     *
     *
     *
     * This is implemented in its own method for ease of subclassing.
     *
     *
     * @param symbol the symbol of the pointer to return
     * @param pos    the part of speech of the pointer to return, can be
     * null unless the pointer symbol is ambiguous
     * @return the pointer corresponding to the specified symbol and part of
     * speech combination
     * @throws NullPointerException     if the symbol is null
     * @throws IllegalArgumentException if the symbol and part of speech combination does not
     * correspond to a known pointer
     * @since JWI 2.1.0
     */
    private fun resolvePointer(symbol: String, pos: POS?): Pointer {
        return getPointerType(symbol, pos)
    }

    companion object {

        // singleton instance
        @JvmStatic
        var instance: DataLineParser? = null
            /**
             * Returns the singleton instance of this class, instantiating it if
             * necessary. The singleton instance will not be null.
             *
             * @return the non-null singleton instance of this class,
             * instantiating it if necessary.
             * @since JWI 2.0.0
             */
            get() {
                if (field == null) {
                    field = DataLineParser()
                }
                return field
            }
            private set
    }
}
