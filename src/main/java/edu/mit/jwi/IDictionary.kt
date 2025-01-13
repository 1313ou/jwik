/* ******************************************************************************
 * Java Wordnet Interface Library (JWI) v2.4.0
 * Copyright (c) 2007-2015 Mark A. Finlayson
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 4.0
 * International Public License, which means it may be freely used for all
 * purposes, as long as proper acknowledgment is made.  See the license file
 * included with this distribution for more details.
 *******************************************************************************/
package edu.mit.jwi

import edu.mit.jwi.data.ContentTypeKey
import edu.mit.jwi.data.IHasCharset
import edu.mit.jwi.data.IHasLifecycle
import edu.mit.jwi.data.compare.ILineComparator
import edu.mit.jwi.item.*
import edu.mit.jwi.item.Word.Companion.checkLexicalId
import java.nio.charset.Charset

/**
 * Objects that implement this interface are intended as the main entry point to
 * accessing Wordnet data. The dictionary must be opened by calling
 * `open()` before it is used, otherwise its methods throw an
 * [IllegalStateException].
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
interface IDictionary : IHasVersion, IHasLifecycle, IHasCharset {

    // C O N F I G

    /**
     * Sets the character set associated with this dictionary. The character set may be null.
     *
     * @param charset the possibly null character set to use when
     * decoding files.
     * @since JWI 2.3.4
     */
    override var charset: Charset?

    /**
     * Sets the comparator associated with this content type in this dictionary.
     * The comparator may be null in which case it is reset to defaults.
     *
     * @param contentTypeKey the content type for which the comparator is to be set.
     * @param comparator the possibly null comparator set to use when decoding files.
     * @throws IllegalStateException if the provider is currently open
     */
    fun setComparator(contentTypeKey: ContentTypeKey, comparator: ILineComparator?)

    /**
     * Sets pattern attached to content type key, that source files have to match to be selected.
     * This gives selection a first opportunity before falling back on standard data type selection.
     *
     * @param contentTypeKey the content type key for which the matcher is to be set.
     * @param pattern regexp pattern
     */
    fun setSourceMatcher(contentTypeKey: ContentTypeKey, pattern: String?)

    /**
     * Configure from config bundle
     */
    fun configure(config: Config?) {
        // default
        charset = Charset.defaultCharset()

        // enforce config
        if (config == null) {
            return
        }

        // global params
        if (config.checkLexicalId != null) {
            checkLexicalId = config.checkLexicalId == true
        }

        // dictionary params
        if (config.indexNounComparator != null) {
            setComparator(ContentTypeKey.INDEX_NOUN, config.indexNounComparator)
        }
        if (config.indexVerbComparator != null) {
            setComparator(ContentTypeKey.INDEX_VERB, config.indexVerbComparator)
        }
        if (config.indexAdjectiveComparator != null) {
            setComparator(ContentTypeKey.INDEX_ADJECTIVE, config.indexAdjectiveComparator)
        }
        if (config.indexAdverbComparator != null) {
            setComparator(ContentTypeKey.INDEX_ADVERB, config.indexAdverbComparator)
        }

        if (config.indexSensePattern != null) {
            setSourceMatcher(ContentTypeKey.SENSE, config.indexSensePattern)
            setSourceMatcher(ContentTypeKey.SENSES, config.indexSensePattern)
        }
        if (config.indexSenseKeyComparator != null) {
            setComparator(ContentTypeKey.SENSE, config.indexSenseKeyComparator)
            setComparator(ContentTypeKey.SENSES, config.indexSenseKeyComparator)
        }
        if (config.charSet != null) {
            charset = config.charSet
        }
    }

    // F I N D

    /**
     * This method is identical to `getIndexWord(IndexWordID)` and
     * is provided as a convenience.
     *
     * @param lemma the lemma for the index word requested; may not be
     * null, empty, or all whitespace
     * @param pos   the part of speech; may not be null
     * @return the index word corresponding to the specified lemma and part of
     * speech, or null if none is found
     * @throws NullPointerException     if either argument is null
     * @throws IllegalArgumentException if the specified lemma is empty or all whitespace
     * @since JWI 1.0
     */
    fun getIndexWord(lemma: String, pos: POS): IndexWord?

    /**
     * Retrieves the specified index word object from the database. If the
     * specified lemma/part of speech combination is not found, returns
     * null.
     *
     *
     * *Note:* This call does no stemming on the specified lemma, it is
     * taken as specified. That is, if you submit the word "dogs", it will
     * search for "dogs", not "dog"; in the standard Wordnet distribution, there
     * is no entry for "dogs" and therefore the call will return
     * null. This is in contrast to the Wordnet API provided by
     * Princeton. If you want your searches to capture morphological variation,
     * use the descendants of the [IStemmer] class.
     *
     * @param id the id of the index word to search for; may not be
     * null
     * @return the index word, if found; null otherwise
     * @throws NullPointerException if the argument is null
     * @since JWI 1.0
     */
    fun getIndexWord(id: IndexWordID): IndexWord?

    /**
     * Returns an iterator that will iterate over all index words of the
     * specified part of speech.
     *
     * @param pos the part of speech over which to iterate; may not be
     * null
     * @return an iterator that will iterate over all index words of the
     * specified part of speech
     * @throws NullPointerException if the argument is null
     * @since JWI 1.0
     */
    fun getIndexWordIterator(pos: POS): Iterator<IndexWord>

    /**
     * Retrieves the word with the specified id from the database. If the
     * specified word is not found, returns null
     *
     * @param id the id of the word to search for; may not be null
     * @return the word, if found; null otherwise
     * @throws NullPointerException if the argument is null
     * @since JWI 1.0
     */
    fun getWord(id: IWordID): Word?

    /**
     * Retrieves the word with the specified sense key from the database. If the
     * specified word is not found, returns null
     *
     * @param key the sense key of the word to search for; may not be
     * null
     * @return the word, if found; null otherwise
     * @throws NullPointerException if the argument is null
     * @since JWI 1.0
     */
    fun getWord(key: SenseKey): Word?

    /**
     * Retrieves the synset with the specified id from the database. If the
     * specified synset is not found, returns null
     *
     * @param id the id of the synset to search for; may not be
     * null
     * @return the synset, if found; null otherwise
     * @throws NullPointerException if the argument is null
     * @since JWI 1.0
     */
    fun getSynset(id: SynsetID): Synset?

    /**
     * Returns an iterator that will iterate over all synsets of the specified
     * part of speech.
     *
     * @param pos the part of speech over which to iterate; may not be
     * null
     * @return an iterator that will iterate over all synsets of the specified
     * part of speech
     * @throws NullPointerException if the argument is null
     * @since JWI 1.0
     */
    fun getSynsetIterator(pos: POS): Iterator<Synset>

    /**
     * Retrieves the sense entry for the specified sense key from the database.
     * If the specified sense key has no associated sense entry, returns
     * null
     *
     * @param key the sense key of the entry to search for; may not be
     * null
     * @return the entry, if found; null otherwise
     * @throws NullPointerException if the argument is null
     * @since JWI 1.0
     */
    fun getSenseEntry(key: SenseKey): SenseEntry?

    /**
     * Returns an iterator that will iterate over all sense entries in the
     * dictionary.
     *
     * @return an iterator that will iterate over all sense entries
     * @since JWI 1.0
     */
    fun getSenseEntryIterator(): Iterator<SenseEntry>

    /**
     * Retrieves the exception entry for the specified surface form and part of
     * speech from the database. If the specified surface form/ part of speech
     * pair has no associated exception entry, returns null
     *
     * @param surfaceForm the surface form to be looked up; may not be null
     * , empty, or all whitespace
     * @param pos         the part of speech; may not be null
     * @return the entry, if found; null otherwise
     * @throws NullPointerException     if either argument is null
     * @throws IllegalArgumentException if the specified surface form is empty or all whitespace
     * @since JWI 1.0
     */
    fun getExceptionEntry(surfaceForm: String, pos: POS): ExceptionEntry?

    /**
     * Retrieves the exception entry for the specified id from the database. If
     * the specified id is not found, returns null
     *
     * @param id the exception entry id of the entry to search for; may not be
     * null
     * @return the exception entry for the specified id
     * @since JWI 1.1
     */
    fun getExceptionEntry(id: ExceptionEntryID): ExceptionEntry?

    /**
     * Returns an iterator that will iterate over all exception entries of the
     * specified part of speech.
     *
     * @param pos the part of speech over which to iterate; may not be
     * null
     * @return an iterator that will iterate over all exception entries of the
     * specified part of speech
     * @throws NullPointerException if the argument is null
     * @since JWI 1.0
     */
    fun getExceptionEntryIterator(pos: POS): Iterator<ExceptionEntry>

    /**
     * Returns list of lemmas that have the given start.
     *
     * @param start start of lemmas searched for; may not be
     * null
     * @param pos   the part of speech over which to iterate; may be
     * null, in which case it ignores pos
     * @param limit maximum number of results, 0 for no limit
     * @return a set of lemmas in dictionary that have given start
     * @throws NullPointerException if the argument is null
     * @since JWIX 2.4.0.4
     */
    fun getWords(start: String, pos: POS?, limit: Int): Set<String>
 }
