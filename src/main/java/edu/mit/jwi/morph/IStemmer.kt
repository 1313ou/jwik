package edu.mit.jwi.morph

import edu.mit.jwi.item.POS

/**
 * A stemmer is an object that can transform surface forms of words into a stem, also known as a root form, base form, or headword.
 */
interface IStemmer {

    /**
     * Takes the surface form of a word, as it appears in the text, and the assigned Wordnet part of speech.
     * The surface form may or may not contain whitespace or underscores, and may be in mixed case.
     * The part of speech may be null, which means that all parts of speech should be considered.
     * Returns a list of stems, in preferred order.
     * No stem should be repeated in the list.
     * If no stems are found, this call returns an empty list.
     *
     * @param surfaceForm the surface form of which to find the stem
     * @param pos the part of speech to find stems for; if null, find stems for all parts of speech
     * @return the list of stems found for the surface form and part of speech combination
     * @throws IllegalArgumentException if the specified surface form is empty or all whitespace
     */
    fun findStems(surfaceForm: String, pos: POS?): List<String>
}
