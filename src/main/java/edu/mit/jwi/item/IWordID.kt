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
 * A unique identifier sufficient to retrieve a particular word from the Wordnet
 * database. Consists of a synset id, sense number, and lemma.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
interface IWordID : IHasPOS, IItemID {

    val synsetID: ISynsetID

    /**
     * Returns the word number, which is a number from 1 to 255 that indicates
     * the order this word is listed in the Wordnet data files. If the word
     * number has not been specified, will return -1. If this method returns -1,
     * the [.getLemma] method will return a non-`null`,
     * non-empty string, non-whitespace string.
     *
     * @return an integer between 1 and 255, inclusive; or -1 if the word number
     * has not been specified.
     * @since JWI 1.0
     */
    val wordNumber: Int

    val lemma: String?
}
