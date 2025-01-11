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
 * A word, which in Wordnet is an index word paired with a synset.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
interface IWord : IHasPOS, IItem<IWordID> {

    val lemma: String

    val synset: ISynset

    val senseKey: ISenseKey

    /**
     * An integer in the closed range [0,15] that, when appended onto lemma,
     * uniquely identifies a sense within a lexicographer file. Lexical id
     * numbers usually start with 0, and are incremented as additional senses of
     * the word are added to the same file, although there is no requirement
     * that the numbers be consecutive or begin with 0. Note that a value of 0
     * is the default, and therefore is not present in lexicographer files. In
     * the wordnet data files the lexical id is represented as a one digit
     * hexadecimal integer.
     *
     * @return the lexical id of the word, an integer between 0 and 15,
     * inclusive
     * @since JWI 1.0
     */
    val lexicalID: Int

    val related: Map<IPointer, List<IWordID>>

    /**
     * Returns an immutable list of all word ids related to this word by the
     * specified pointer type. Note that this only returns words related by
     * lexical pointers (i.e., not semantic pointers). To retrieve items related
     * by semantic pointers, call [ISynset.getRelatedFor]. If this
     * word has no targets for the specified pointer, this method
     * returns an empty list. This method never returns null.
     *
     * @param ptr the pointer for which related words are requested
     * @return the list of words related by the specified pointer, or an empty
     * list if none.
     * @since JWI 2.0.0
     */
    fun getRelatedWords(ptr: IPointer): List<IWordID>

    val relatedWords: List<IWordID>?

    val verbFrames: List<IVerbFrame>?

    val adjectiveMarker: AdjMarker?
}
