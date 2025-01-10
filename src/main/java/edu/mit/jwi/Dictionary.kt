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
import edu.mit.jwi.data.FileProvider
import edu.mit.jwi.item.Word.Companion.checkLexicalId
import java.io.File
import java.net.URL
import java.nio.charset.Charset

/**
 * Basic `IDictionary` implementation that mounts files on disk and has
 * caching. A file URL to the directory containing the Wordnet dictionary files
 * must be provided.  This implementation has adjustable caching.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
class Dictionary : CachingDictionary {

    /**
     * Constructs a new dictionary that uses the Wordnet files located in a
     * directory pointed to by the specified url
     *
     * @param wordnetDir an url pointing to a directory containing the wordnet data
     * files on the filesystem
     * @param config     config parameters
     * @since JWI 1.0
     */
    @JvmOverloads
    constructor(wordnetDir: URL, config: Config? = null) : super(DataSourceDictionary(FileProvider(wordnetDir))) {
        configure(config)
    }

    /**
     * Constructs a new dictionary that uses the Wordnet files located in a
     * directory pointed to by the specified file
     *
     * @param wordnetDir a file pointing to a directory containing the wordnet data files on the filesystem
     * @param config     config parameters
     * @throws NullPointerException if the specified file is `null`
     * @since JWI 1.0
     */
    @JvmOverloads
    constructor(wordnetDir: File, config: Config? = null) : super(DataSourceDictionary(FileProvider(wordnetDir))) {
        configure(config)
    }

    private fun configure(config: Config?) {
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
}
