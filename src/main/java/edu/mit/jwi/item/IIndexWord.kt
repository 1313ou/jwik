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
 * A Wordnet index word object, represented in the Wordnet files as a line in an index file.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
interface IIndexWord : IHasPOS, IItem<IIndexWordID> {

    /**
     * The lemma (word root) associated with this index word.
     * Never empty or all whitespace.
     * @since JWI 1.0
     */
    val lemma: String

    val wordIDs: List<IWordID>

    /**
     * The number of senses of lemma that are ranked according to their frequency of occurrence in semantic concordance texts.
     * This will be a non-negative number.
     * @since JWI 2.1.2
     */
    val tagSenseCount: Int

    /**
     * An immutable set containing all the different types of pointers that this index word has across all synsets containing this word.
     * If all senses of the word have no pointers, this method returns an empty set.
     * @since JWI 2.3.0
     */
    val pointers: Set<Pointer>
}
