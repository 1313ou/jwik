package edu.mit.jwi.data.parse

import edu.mit.jwi.item.*
import edu.mit.jwi.item.LexFile.Companion.getLexicalFile
import edu.mit.jwi.item.POS.Companion.getPartOfSpeech
import edu.mit.jwi.item.Pointer.Companion.getPointerType
import edu.mit.jwi.item.Synset.WordBuilder
import edu.mit.jwi.item.VerbFrame.Companion.getFrame
import java.util.*

/**
 * Parser for Wordnet data files (e.g., `data.adv` or `adv.dat`).
 * This parser produces a Synset object.
 */
object DataLineParser : ILineParser<Synset> {

    override fun parseLine(line: String): Synset {

        try {
            val tokenizer = StringTokenizer(line, " ")

            // Get offset
            val offset = tokenizer.nextToken().toInt()

            // Consume lex_filenum
            val lexFilenum = tokenizer.nextToken().toInt()
            val lexFile = resolveLexicalFile(lexFilenum)

            // Get part of speech
            val synsetTag = tokenizer.nextToken()[0]
            val synsetPos = getPartOfSpeech(synsetTag)

            // ID
            val synsetID = SynsetID(offset, synsetPos)

            // Determine if it is an adjective satellite
            val isAdjSat = (synsetTag == 's')

            // A synset is an adjective head if it is the 00 lexical file, is not an adjective satellite, and it has an antonym.
            // The Wordnet definition says head synsets have to have an antonym, but this is actually violated (perhaps mistakenly) in a small number of cases,
            // e.g., in Wordnet 3.0:
            // 01380267 aerial (no antonyms), with satellite 01380571 free-flying
            // 01380721 marine (no antonyms), with satellite 01380926 deep-sea
            val isAdjHead = !isAdjSat && lexFilenum == 0

            // Get word count
            val wordCount = tokenizer.nextToken().toInt(16)

            // Get words
            val wordProxies: Array<WordBuilder> = Array<WordBuilder>(wordCount) {
                // Consume next word
                var lemma = tokenizer.nextToken()

                // If it is an adjective, it may be followed by a marker
                var marker: AdjMarker? = null
                if (synsetPos == POS.ADJECTIVE) {
                    for (adjMarker in AdjMarker.entries) {
                        if (lemma.endsWith(adjMarker.symbol)) {
                            marker = adjMarker
                            lemma = lemma.substring(0, lemma.length - adjMarker.symbol.length)
                        }
                    }
                }

                // Parse lex_id
                val lexID: Int = tokenizer.nextToken().toInt(16)

                WordBuilder(it + 1, lemma, lexID, marker)
            }

            // Get pointers
            var synsetPointerMap: MutableMap<Pointer, ArrayList<SynsetID>>? = null
            val pointerCount = tokenizer.nextToken().toInt()
            repeat(pointerCount) {

                // get pointer symbol
                val pointerType: Pointer = resolvePointer(tokenizer.nextToken(), synsetPos)
                checkNotNull(pointerType)

                // get synset target offset
                val targetOffset: Int = tokenizer.nextToken().toInt()

                // get target synset part of speech
                val targetPos = getPartOfSpeech(tokenizer.nextToken()[0])

                // ID
                var targetSynsetID = SynsetID(targetOffset, targetPos)

                // get source/target numbers
                var sourceTargetNum: Int = tokenizer.nextToken().toInt(16)

                // this is a semantic pointer if the source/target numbers are zero
                if (sourceTargetNum == 0) {
                    if (synsetPointerMap == null) {
                        synsetPointerMap = HashMap<Pointer, ArrayList<SynsetID>>()
                    }
                    var pointers = synsetPointerMap.computeIfAbsent(pointerType) { _: Pointer -> ArrayList<SynsetID>() }
                    pointers.add(targetSynsetID)
                } else {
                    // this is a lexical pointer
                    val sourceNum: Int = sourceTargetNum / 256
                    val targetNum: Int = sourceTargetNum and 255
                    val targetWordID: SenseID = SenseIDWithNum(targetSynsetID, targetNum)
                    wordProxies[sourceNum - 1].addRelatedWord(pointerType, targetWordID)
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
            if (synsetPos == POS.VERB) {
                val peekTok = tokenizer.nextToken()
                if (!peekTok.startsWith("|")) {
                    val verbFrameCount = peekTok.toInt()
                    repeat(verbFrameCount) {
                        // Consume '+'
                        tokenizer.nextToken()
                        // Get frame number
                        var frameNum: Int = tokenizer.nextToken().toInt()
                        var frame: VerbFrame = resolveVerbFrame(frameNum)
                        // Get word number
                        val wordNum: Int = tokenizer.nextToken().toInt(16)
                        if (wordNum > 0) {
                            wordProxies[wordNum - 1].addVerbFrame(frame)
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

            // create members
            val words = listOf<WordBuilder>(*wordProxies)

            // create synset
            return Synset(synsetID, lexFile, isAdjSat, isAdjHead, gloss, words, synsetPointerMap)
        } catch (e: NumberFormatException) {
            throw MisformattedLineException(line, e)
        } catch (e: NoSuchElementException) {
            throw MisformattedLineException(line, e)
        }
    }

    /**
     * Retrieves the verb frames for the parseLine method.
     * This is implemented in its own method for ease of subclassing.
     * @param frameNum the number of the frame to return
     * @return the verb frame corresponding to the specified frame number, or null if there is none
     */
    private fun resolveVerbFrame(frameNum: Int): VerbFrame {
        return getFrame(frameNum)!!
    }

    /**
     * Retrieves the lexical file objects for the parseLine method.
     * If the lexical file number does correspond to a known lexical file, the method returns a singleton placeholder 'unknown' lexical file object.
     * This is implemented in its own method for ease of subclassing.
     * @param lexFileNum the number of the lexical file to return
     * @return the lexical file corresponding to the specified frame number
     */
    private fun resolveLexicalFile(lexFileNum: Int): LexFile {
        return getLexicalFile(lexFileNum)
    }

    /**
     * Retrieves the pointer objects for the parseLine method.
     * This is implemented in its own method for ease of subclassing.
     *
     * @param symbol the symbol of the pointer to return
     * @param pos the part of speech of the pointer to return, can be null unless the pointer symbol is ambiguous
     * @return the pointer corresponding to the specified symbol and part of speech combination
     * @throws IllegalArgumentException if the symbol and part of speech combination does not correspond to a known pointer
     */
    private fun resolvePointer(symbol: String, pos: POS?): Pointer {
        return getPointerType(symbol, pos)
    }
}
