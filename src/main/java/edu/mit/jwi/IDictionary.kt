package edu.mit.jwi

import edu.mit.jwi.data.IHasLifecycle
import edu.mit.jwi.item.*

/**
 * Objects that implement this interface are intended as the main entry point to accessing Wordnet data.
 * The dictionary must be opened by calling open() before it is used, otherwise its methods throw an IllegalStateException.
 */
interface IDictionary : IHasVersion, IHasLifecycle {

    // C O N F I G

    /**
     * Configure from config bundle
     */
    fun configure(config: Config?)

    // L O O K   U P

    /**
     * This method is identical to getIndex(IndexID) and is provided as a convenience.
     *
     * @param lemma the lemma for the index requested; may not be empty or all whitespace
     * @param pos the part of speech
     * @return the index corresponding to the specified lemma and part of speech, or null if none is found
     * @throws IllegalArgumentException if the specified lemma is empty or all whitespace
     */
    fun getIndex(lemma: String, pos: POS): Index?

    /**
     * Retrieves the specified index object from the database.
     * If the specified lemma/part of speech combination is not found, returns null.
     *
     * *Note:* This call does no stemming on the specified lemma, it is taken as specified.
     * That is, if you submit the word "dogs", it will search for "dogs", not "dog" in the standard Wordnet distribution, there is no entry for "dogs" and therefore the call will return null.
     * This is in contrast to the Wordnet API provided by Princeton.
     * If you want your searches to capture morphological variation, use the descendants of the Stemmer class.
     *
     * @param id the id of the index to search for
     * @return the index, if found; null otherwise
     */
    fun getIndex(id: IndexID): Index?

    /**
     * Retrieves the sense with the specified id from the database. If the specified sense is not found, returns null
     *
     * @param id the id of the sense to search for
     * @return the sense, if found; null otherwise
     */
    fun getSense(id: SenseID): Sense?

    /**
     * Retrieves the sense with the specified sense key from the database.
     * If the specified sense is not found, returns null
     *
     * @param key the sense key of the sense to search for
     * @return the sense, if found; null otherwise
     */
    fun getSense(key: SenseKey): Sense?

    /**
     * Retrieves the synset with the specified id from the database. If the specified synset is not found, returns null
     *
     * @param id the id of the synset to search for
     * @return the synset, if found; null otherwise
     */
    fun getSynset(id: SynsetID): Synset?

    /**
     * Retrieves the sense entry for the specified sense key from the database.
     * If the specified sense key has no associated sense entry, returns null
     *
     * @param key the sense key of the entry to search for
     * @return the entry, if found; null otherwise
     */
    fun getSenseEntry(key: SenseKey): SenseEntry?

    /**
     * Retrieves the exception entry for the specified surface form and part of speech from the database.
     * If the specified surface form / part of speech pair has no associated exception entry, returns null
     *
     * @param surfaceForm the surface form to be looked up; may not be empty or all whitespace
     * @param pos the part of speech
     * @return the entry, if found; null otherwise
     */
    fun getExceptionEntry(surfaceForm: String, pos: POS): ExceptionEntry?

    /**
     * Retrieves the exception entry for the specified id from the database. If the specified id is not found, returns null
     *
     * @param id the exception entry id of the entry to search for
     * @return the exception entry for the specified id
     */
    fun getExceptionEntry(id: ExceptionEntryID): ExceptionEntry?

    // I T E R A T O R S

    /**
     * Returns an iterator that will iterate over all indexes of the specified part of speech.
     *
     * @param pos the part of speech over which to iterate
     * @return an iterator that will iterate over all indexes of the specified part of speech
     */
    fun getIndexIterator(pos: POS): Iterator<Index>

    /**
     * Returns an iterator that will iterate over all sense entries in the dictionary.
     *
     * @return an iterator that will iterate over all sense entries
     */
    fun getSenseEntryIterator(): Iterator<SenseEntry>

    /**
     * Returns an iterator that will iterate over all synsets of the specified part of speech.
     *
     * @param pos the part of speech over which to iterate
     * @return an iterator that will iterate over all synsets of the specified part of speech
     */
    fun getSynsetIterator(pos: POS): Iterator<Synset>

    /**
     * Returns an iterator that will iterate over all exception entries of the specified part of speech.
     *
     * @param pos the part of speech over which to iterate
     * @return an iterator that will iterate over all exception entries of the specified part of speech
     */
    fun getExceptionEntryIterator(pos: POS): Iterator<ExceptionEntry>

    // S T A R T S

    /**
     * Returns list of lemmas that have the given start.
     *
     * @param start start of lemmas searched for
     * @param pos the part of speech over which to iterate; may be null, in which case it ignores pos
     * @param limit maximum number of results, 0 for no limit
     * @return a set of lemmas in dictionary that have given start
     */
    fun getLemmasStartingWith(start: String, pos: POS? = null, limit: Int = 0): Set<String>
}
