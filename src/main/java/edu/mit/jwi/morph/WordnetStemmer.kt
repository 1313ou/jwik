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
     * The dictionary in use by the stemmer; will not return null
     *
     * @return the dictionary in use by this stemmer
     */
    val dictionary: IDictionary,
) : SimpleStemmer() {

    override fun findStems(word: String, pos: POS?): List<String> {
        var word = word
        word = normalize(word)

        if (pos == null) {
            return super.findStems(word, null)
        }

        val result: MutableSet<String> = LinkedHashSet<String>()

        // first look for the word in the exception lists
        val excEntry = dictionary.getExceptionEntry(word, pos)
        if (excEntry != null) {
            result.addAll(excEntry.rootForms)
        }

        // then look and see if it's in Wordnet; if so, the form itself is a stem
        if (dictionary.getIndexWord(word, pos) != null) {
            result.add(word)
        }

        if (excEntry != null) {
            return ArrayList<String>(result)
        }

        // go to the simple stemmer and check and see if any of those stems are in WordNet
        val possibles: MutableList<String> = super.findStems(word, pos) as MutableList<String>

        // Fix for Bug015: don't allow empty strings to go to the dictionary
        possibles.removeIf { s: String? -> s!!.trim { it <= ' ' }.isEmpty() }

        // check each algorithmically obtained root to see if it's in WordNet
        for (possible in possibles) {
            if (dictionary.getIndexWord(possible, pos) != null) {
                result.add(possible)
            }
        }

        if (result.isEmpty()) {
            return if (possibles.isEmpty()) {
                emptyList<String>()
            } else {
                ArrayList<String>(possibles)
            }
        }
        return ArrayList<String>(result)
    }
}
