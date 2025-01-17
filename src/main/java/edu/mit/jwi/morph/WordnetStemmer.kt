package edu.mit.jwi.morph

import edu.mit.jwi.IDictionary
import edu.mit.jwi.item.POS

/**
 * This stemmer adds functionality to the simple pattern-based stemmer SimpleStemmer by checking to see if possible stems are actually contained in Wordnet.
 * If any stems are found, only these stems are returned.
 * If no prospective stems are found, the word is considered unknown, and the result returned is the same as that of the `SimpleStemmer` class.
 */
class WordnetStemmer(
    /**
     * The dictionary in use by the stemmer
     *
     * @return the dictionary in use by this stemmer
     */
    val dictionary: IDictionary,
) : SimpleStemmer() {

    override fun findStems(word: String, pos: POS?): List<String> {
        var word = normalize(word)
        if (pos == null) {
            return POS.entries
                .flatMap { findStems(word, it) }
                .distinct()
        }

        // look and see if it's in Wordnet
        // if so, the form itself is a stem
        var self = dictionary.getIndexWord(word, pos) != null

        // first look for the word in the exception lists
        val excEntry = dictionary.getExceptionEntry(word, pos)
        if (excEntry != null) {
            val result = ArrayList<String>()
            if (self)
                result.add(word)
            result.addAll(excEntry.rootForms)
            return result.distinct()
        }

        // go to the simple stemmer and check and see if any of those stems are in WordNet
        val result = super.findStems(word, pos)
            .map { it.trim { it <= ' ' } }
            .filterNot { it.isEmpty() }
            .filter { dictionary.getIndexWord(it, pos) != null }
            .toMutableList()
        if (self)
            result.add(0, word)
        return result.distinct()
    }
}
