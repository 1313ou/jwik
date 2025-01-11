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

/**
 * Represents a synset.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0, Nov. 16, 2007
 * @since JWI 1.0
 */
interface ISynset : IHasPOS, IItem<ISynsetID> {

    /**
     * The data file byte offset of this synset in the associated data source
     * @since JWI 1.0
     */
    val offset: Int

    /**
     * The lexical file it was found in
     */
    val lexicalFile: ILexFile

    /**
     * The type of the synset, encoded as follows:
     * 1=Noun,
     * 2=Verb,
     * 3=Adjective,
     * 4=Adverb,
     * 5=Adjective Satellite.
     * @since JWI 1.0
     */
    val type: Int

    /**
     * The gloss or definition that comes with the synset
     */
    val gloss: String

    /**
     * The words that are members of the synset
     */
    val words: List<IWord>

    /**
     * true if this synset is / represents an adjective head; false otherwise.
     * @since JWI 1.0
     */
    val isAdjectiveHead: Boolean

    /**
     * Returns true if this synset is / represents an adjective satellite; false otherwise.
     * @since JWI 1.0
     */
    val isAdjectiveSatellite: Boolean

    /**
     * Semantic relations
     */
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
     * @param ptrType the pointer for which related synsets are to be retrieved.
     * @return the list of synsets related by the specified pointer; if there are no such synsets, returns the empty list
     * @since JWI 2.0.0
     */
    fun getRelatedSynsets(ptrType: IPointer): List<ISynsetID> {
        return relatedMap[ptrType] ?: emptyList()
    }

    val relatedSynsets: List<ISynsetID>
        get() = relatedMap.values
            .flatMap { it.toList() }
            .distinct()
            .toList()

}
