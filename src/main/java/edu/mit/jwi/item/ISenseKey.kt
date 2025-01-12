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

import java.io.Serializable

/**
 * A sense key is a unique string that identifies a Wordnet word (an
 * [Word]). The canonical string representation is:
 * <pre>
 * lemma%ss_type:lex_filenum:lex_id:head_word:head_id
</pre> *
 * To transform a [String] representation of a sense key into an actual
 * sense key, use the [SenseKeyParser] class.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.1.0
 */
interface ISenseKey : IHasPOS, Comparable<ISenseKey>, Serializable {

    val lemma: String

    /**
     * Returns the synset type for the key. The synset type is a one digit
     * decimal integer representing the synset type for the sense.
     * <pre>
     * 1=NOUN
     * 2=VERB
     * 3=ADJECTIVE
     * 4=ADVERB
     * 5=ADJECTIVE SATELLITE
     * </pre>
     *
     * @return the synset type, an integer between 1 and 5, inclusive
     * @since JWI 2.1.0
     */
    val synsetType: Int

    /**
     * Returns true if this sense key points to an adjective
     * satellite; false otherwise.
     *
     * @return true if this sense key points to an adjective
     * satellite; false otherwise
     * @since JWI 2.1.0
     */
    val isAdjectiveSatellite: Boolean

    val lexicalFile: ILexFile?

    /**
     * Returns the lexical id for this sense key, which is a non-negative
     * integer.
     *
     * @return the non-negative lexical id for this sense key
     * @since JWI 2.1.0
     */
    val lexicalID: Int

    val headWord: String?

    /**
     * Returns the head id for this sense key. The head id is only present if
     * the sense is an adjective satellite synset, and is a two digit decimal
     * integer that, when appended onto the head word, uniquely identifies the
     * sense within a lexicographer file. If this sense key is not for an
     * adjective synset, this method returns `-1`.
     *
     * @return the head id for this adjective satellite synset, or
     * `-1` if the indicated sense is not an adjective
     * satellite
     * @since JWI 2.1.0
     */
    val headID: Int

    /**
     * This method is used to set the head for sense keys for adjective
     * satellites, and it can only be called once, directly after the relevant
     * word is created. If this method is called on a sense key that has had its
     * head set already, or is not an adjective satellite, it will throw an
     * exception.
     *
     * @param headLemma the head lemma to be set
     * @param headLexID the head lexid to be set
     * @throws IllegalStateException if this method has already been called, if the headLemma is
     * empty or all whitespace or if the headLexID is illegal.
     * @throws NullPointerException  if the headLemma is null
     * @since JWI 2.1.0
     */
    fun setHead(headLemma: String, headLexID: Int)

    /**
     * This method will always return false if the
     * [.isAdjectiveSatellite] returns false. If that
     * method returns true, this method will only return
     * true if [.setHead] has not yet been
     * called.
     *
     * @return true if the head lemma and lexical id need to be
     * set; false otherwise.
     * @since JWI 2.1.0
     */
    fun needsHeadSet(): Boolean
}
