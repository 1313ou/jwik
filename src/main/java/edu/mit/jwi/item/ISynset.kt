/* ******************************************************************************
 * Java Wordnet Interface Library (JWI) v2.4.0
 * Copyright (c) 2007-2015 Mark A. Finlayson
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 4.0
 * International Public License, which means it may be freely used for all
 * purposes, as long as proper acknowledgment is made.  See the license file
 * included with this distribution for more details.
 *******************************************************************************/
package edu.mit.jwi.item

import edu.mit.jwi.NonNull
import edu.mit.jwi.Nullable

/**
 * Represents a synset.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0, Nov. 16, 2007
 * @since JWI 1.0
 */
interface ISynset : IHasPOS, IItem<ISynsetID> {

    /**
     * Returns the data file byte offset of this synset.
     *
     * @return int the offset in the associated data source
     * @since JWI 1.0
     */
    val offset: Int

     val lexicalFile: ILexFile

    /**
     * Returns the type of the synset, encoded as follows: 1=Noun, 2=Verb,
     * 3=Adjective, 4=Adverb, 5=Adjective Satellite.
     *
     * @return the type of the synset, an integer between 1 and 5, inclusive
     * @since JWI 1.0
     */
    val type: Int

    val gloss: String

    val words: List<IWord>

    /**
     * Returns the word with the specified word number. Words are numbered
     * sequentially from 1 up to, and including, 255.
     *
     * @param wordNumber the number of the word to be retrieved
     * @return the word with the specified word number
     * @throws IndexOutOfBoundsException if the word number is not an appropriate word number for this
     * synset.
     * @since JWI 2.1.2
     */
    fun getWord(wordNumber: Int): IWord

    /**
     * Returns `true` if this synset is an adjective head;
     * `false` otherwise.
     *
     * @return `true` if this synset represents an adjective head;
     * `false` otherwise.
     * @since JWI 1.0
     */
     val isAdjectiveHead: Boolean

    /**
     * Returns `true` if this synset is an adjective satellite;
     * `false` otherwise.
     *
     * @return `true` if this synset represents an adjective satellite;
     * `false` otherwise.
     * @since JWI 1.0
     */
    val isAdjectiveSatellite: Boolean

    val relatedMap: Map<IPointer, List<ISynsetID>>

    /**
     * Returns an immutable list of the ids of all synsets that are related to
     * this synset by the specified pointer type. Note that this only returns a
     * non-empty result for semantic pointers (i.e., non-lexical pointers). To
     * obtain lexical pointers, call [IWord.getRelatedWords] on the
     * appropriate object.
     *
     * If there are no such synsets, this method returns the empty list.
     *
     * @param ptr the pointer for which related synsets are to be retrieved.
     * @return the list of synsets related by the specified pointer; if there
     * are no such synsets, returns the empty list
     * @since JWI 2.0.0
     */
    fun getRelatedSynsets(ptr: IPointer): List<ISynsetID>

    val relatedSynsets: List<ISynsetID?>
}
