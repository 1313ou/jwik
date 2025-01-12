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

import edu.mit.jwi.data.*
import edu.mit.jwi.data.IHasLifecycle.ObjectClosedException
import edu.mit.jwi.data.compare.ILineComparator
import edu.mit.jwi.data.parse.ILineParser
import edu.mit.jwi.item.*
import edu.mit.jwi.item.Synset.Companion.zeroFillOffset
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import kotlin.Throws

/**
 * Basic implementation of the `IDictionary` interface. A path to the
 * Wordnet dictionary files must be provided. If no `IDataProvider` is
 * specified, it uses the default implementation provided with the distribution.
 *
 * Constructs a dictionary with a caller-specified `IDataProvider`.
 *
 * @param dataProvider data provider
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.2.0
 */
class DataSourceDictionary(override val dataProvider: IDataProvider) : IDataSourceDictionary {

    override val version: IVersion?
        get() {
            checkOpen()
            return dataProvider.version
        }

    @Throws(IOException::class)
    override fun open(): Boolean {
        return dataProvider.open()
    }

    override fun close() {
        dataProvider.close()
    }

    override val isOpen: Boolean
        get() = dataProvider.isOpen

    /**
     * An internal method for assuring compliance with the dictionary interface
     * that says that methods will throw `ObjectClosedException`s if
     * the dictionary has not yet been opened.
     *
     * @throws ObjectClosedException if the dictionary is closed.
     */
    private fun checkOpen() {
        if (!isOpen) {
            throw ObjectClosedException()
        }
    }

    override var charset: Charset?
        get() = dataProvider.charset
        set(charset) {
            dataProvider.charset = charset
        }

    override fun setComparator(contentTypeKey: ContentTypeKey, comparator: ILineComparator?) {
        dataProvider.setComparator(contentTypeKey, comparator)
    }

    override fun setSourceMatcher(contentTypeKey: ContentTypeKey, pattern: String?) {
        dataProvider.setSourceMatcher(contentTypeKey, pattern)
    }

    override fun getIndexWord(lemma: String, pos: POS): IIndexWord? {
        checkOpen()
        return getIndexWord(IndexWordID(lemma, pos))
    }

    override fun getIndexWord(id: IIndexWordID): IIndexWord? {
        checkOpen()
        val content = dataProvider.resolveContentType<IIndexWord>(DataType.INDEX, id.pOS)
        val file: IDataSource<*> = checkNotNull(dataProvider.getSource<IIndexWord>(content!!))
        val line = file.getLine(id.lemma)
        if (line == null) {
            return null
        }
        checkNotNull(content)
        val dataType = content.dataType
        val parser = checkNotNull(dataType.parser)
        return parser.parseLine(line)
    }

    override fun getWords(start: String, pos: POS?, limit: Int): Set<String> {
        checkOpen()
        val result: MutableSet<String> = TreeSet<String>()
        if (pos != null) {
            getWords(start, pos, limit, result)
        } else {
            for (pos2 in POS.entries) {
                getWords(start, pos2, limit, result)
            }
        }
        return result
    }

    private fun getWords(start: String, pos: POS, limit: Int, result: MutableSet<String>): MutableCollection<String> {
        checkOpen()
        val content = checkNotNull(dataProvider.resolveContentType<IIndexWord>(DataType.WORD, pos))
        val dataType = content.dataType
        val parser: ILineParser<IIndexWord> = checkNotNull(dataType.parser)
        val file: IDataSource<*> = checkNotNull(dataProvider.getSource<IIndexWord>(content))
        var found = false
        val lines = file.iterator(start)
        while (lines.hasNext()) {
            val line = lines.next()
            if (line != null) {
                val match = line.startsWith(start)
                if (match) {
                    val index = parser.parseLine(line)
                    val lemma = index.lemma
                    result.add(lemma)
                    found = true
                } else if (found) {
                    break
                }
                if (limit > 0 && result.size >= limit) {
                    break
                }
            }
        }
        return result
    }

    override fun getWord(id: IWordID): Word? {
        checkOpen()
        val synset = getSynset(id.synsetID)
        if (synset == null) {
            return null
        }
        return when (id) {
            is WordNumID   -> synset.words[id.wordNumber - 1]
            is WordLemmaID -> synset.words.first { it.lemma.equals(id.lemma, ignoreCase = true) }
            else           -> throw IllegalArgumentException("Not enough information in IWordID instance to retrieve word.")
        }
    }

    override fun getWord(key: SenseKey): Word? {
        checkOpen()

        // no need to cache result from the following calls as this will have been
        // done in the call to getSynset()
        val entry = getSenseEntry(key)
        if (entry != null) {
            val synset = getSynset(SynsetID(entry.offset, entry.pOS!!))
            if (synset != null) {
                for (synonym in synset.words) {
                    if (synonym.senseKey == key) {
                        return synonym
                    }
                }
            }
        }

        var word: Word? = null

        // sometimes the sense.index file doesn't have the sense key entry
        // so try an alternate method of retrieving words by sense keys
        // We have to search the synonyms of the words returned from the
        // index word search because some synsets have lemmas that differ only in case
        // e.g., {earth, Earth} or {south, South}, and so separate entries
        // are not found in the index file
        val indexWord = getIndexWord(key.lemma, key.pOS!!)
        if (indexWord != null) {
            var possibleWord: Word?
            for (wordID in indexWord.wordIDs) {
                possibleWord = getWord(wordID)
                if (possibleWord != null) {
                    val synset = checkNotNull(possibleWord.synset)
                    val words: List<Word> = synset.words
                    for (synonym in words) {
                        if (synonym.senseKey == key) {
                            word = synonym
                            val lemma = checkNotNull(synonym.lemma)
                            if (lemma == key.lemma) {
                                return synonym
                            }
                        }
                    }
                }
            }
        }
        return word
    }

    override fun getSenseEntry(key: SenseKey): ISenseEntry? {
        checkOpen()
        val content = dataProvider.resolveContentType<ISenseEntry>(DataType.SENSE, null)
        val file = checkNotNull(dataProvider.getSource<ISenseEntry>(content!!))
        val line = file.getLine(key.toString())
        if (line == null) {
            return null
        }
        checkNotNull(content)
        val dataType = content.dataType
        val parser = checkNotNull(dataType.parser)
        return parser.parseLine(line)
    }

    fun getSenseEntries(key: SenseKey): Array<ISenseEntry>? {
        checkOpen()
        val content = dataProvider.resolveContentType<Array<ISenseEntry>>(DataType.SENSES, null)
        val file = checkNotNull(dataProvider.getSource<Array<ISenseEntry>>(content!!))
        val line = file.getLine(key.toString())
        if (line == null) {
            return null
        }
        checkNotNull(content)
        val dataType = content.dataType
        val parser = checkNotNull(dataType.parser)
        return parser.parseLine(line)
    }

    override fun getSynset(id: SynsetID): Synset? {
        checkOpen()
        val content = dataProvider.resolveContentType<Synset>(DataType.DATA, id.pOS)
        val file = dataProvider.getSource<Synset>(content!!)
        val zeroFilledOffset = zeroFillOffset(id.offset)
        checkNotNull(file)
        val line = file.getLine(zeroFilledOffset)
        if (line == null) {
            return null
        }
        checkNotNull(content)
        val dataType = content.dataType
        val parser = checkNotNull(dataType.parser)
        val result = parser.parseLine(line)
        if (result != null) {
            setHeadWord(result)
        }
        return result
    }

    /**
     * This method sets the head word on the specified synset by searching in
     * the dictionary to find the head of its cluster. We will assume the head
     * is the first adjective head synset related by an '&amp;' pointer (SIMILAR_TO)
     * to this synset.
     *
     * @param synset synset
     */
    private fun setHeadWord(synset: Synset) {
        // head words are only needed for adjective satellites
        if (!synset.isAdjectiveSatellite) {
            return
        }

        // go find the head word
        var headSynset: Synset?
        var headWord: Word? = null
        val related: List<SynsetID> = synset.getRelatedFor(Pointer.SIMILAR_TO)
        for (simID in related) {
            headSynset = getSynset(simID)
            // assume first 'similar' adjective head is the right one
            checkNotNull(headSynset)
            if (headSynset.isAdjectiveHead) {
                headWord = headSynset.words[0]
                break
            }
        }
        if (headWord == null) {
            return
        }

        // set head word, if we found it
        var headLemma = headWord.lemma

        // version 1.6 of Wordnet adds the adjective marker symbol
        // on the end of the head word lemma
        val ver = version
        val isVer16 = (ver != null) && (ver.majorVersion == 1 && ver.minorVersion == 6)
        if (isVer16 && headWord.adjectiveMarker != null) {
            headLemma += headWord.adjectiveMarker!!.symbol
        }

        // set the head word for each word
        for (word in synset.words) {
            if (word.senseKey.needsHeadSet()) {
                word.senseKey.setHead(headLemma, headWord.lexicalID)
            }
        }
    }

    override fun getExceptionEntry(surfaceForm: String, pos: POS): IExceptionEntry? {
        return getExceptionEntry(ExceptionEntryID(surfaceForm, pos))
    }

    override fun getExceptionEntry(id: IExceptionEntryID): IExceptionEntry? {
        checkOpen()
        val content = dataProvider.resolveContentType<IExceptionEntryProxy>(DataType.EXCEPTION, id.pOS)
        val file = dataProvider.getSource<IExceptionEntryProxy>(content!!)
        // fix for bug 010
        if (file == null) {
            return null
        }
        val line = file.getLine(id.surfaceForm)
        if (line == null) {
            return null
        }
        checkNotNull(content)
        val dataType = content.dataType
        val parser = checkNotNull(dataType.parser)
        val proxy = parser.parseLine(line)
        if (proxy == null) {
            return null
        }
        return ExceptionEntry(proxy, id.pOS!!)
    }

    override fun getIndexWordIterator(pos: POS): Iterator<IIndexWord> {
        checkOpen()
        return IndexFileIterator(pos)
    }

    override fun getSynsetIterator(pos: POS): Iterator<Synset> {
        checkOpen()
        return DataFileIterator(pos)
    }

    override fun getExceptionEntryIterator(pos: POS): Iterator<IExceptionEntry> {
        checkOpen()
        return ExceptionFileIterator(pos)
    }

    override fun getSenseEntryIterator(): Iterator<ISenseEntry> {
        checkOpen()
        return SenseEntryFileIterator()
    }

    /**
     * Abstract class used for iterating over line-based files.
     */
    abstract inner class FileIterator<T, N> @JvmOverloads constructor(content: IContentType<T>, startKey: String? = null) : Iterator<N>, IHasPOS {

        protected val fFile: IDataSource<T>?

        protected var iterator: Iterator<String>? = null

        protected val fParser: ILineParser<T>

        var currentLine: String? = null
            protected set

        init {
            checkNotNull(dataProvider)
            this.fFile = dataProvider.getSource<T>(content)
            val dataType = content.dataType
            this.fParser = dataType.parser
            iterator = fFile?.iterator(startKey) ?: Collections.emptyIterator<String>() // Fix for Bug018
        }

        override val pOS: POS
            get() {
                val contentType = fFile!!.contentType
                return contentType.pOS!!
            }

        override fun hasNext(): Boolean {
            return iterator!!.hasNext()
        }

        override fun next(): N {
            currentLine = iterator!!.next()
            return parseLine(currentLine!!)!!
        }

        /**
         * Parses the line using a parser provided at construction time
         *
         * @param line line
         * @return parsed object
         */
        abstract fun parseLine(line: String): N
    }

    /**
     * A file iterator where the data type returned by the iterator is the same
     * as that returned by the backing data source.
     *
     * @author Mark A. Finlayson
     * @since JWI 2.1.5
     */
    abstract inner class FileIterator2<T> : FileIterator<T, T> {

        /**
         * Constructs a new file iterator with the specified content type.
         *
         * @param content content type
         * @since JWI 2.1.5
         */
        constructor(content: IContentType<T>) : super(content)

        /**
         * Constructs a new file iterator with the specified content type and start key.
         *
         * @param content  content type
         * @param startKey start key
         * @since JWI 2.1.5
         */
        constructor(content: IContentType<T>, startKey: String) : super(content, startKey)
    }

    /**
     * Iterates over index files.
     */
    inner class IndexFileIterator @JvmOverloads constructor(pos: POS, pattern: String = "") : FileIterator2<IIndexWord>(
        dataProvider.resolveContentType<IIndexWord>(DataType.INDEX, pos)!!,
        pattern
    ) {

        override fun parseLine(line: String): IIndexWord {
            checkNotNull(fParser)
            return fParser.parseLine(line)
        }
    }

    /**
     * Iterates over the sense file.
     */
    inner class SenseEntryFileIterator : FileIterator2<ISenseEntry>(
        dataProvider.resolveContentType<ISenseEntry>(DataType.SENSE, null)!!
    ) {

        override fun parseLine(line: String): ISenseEntry {
            checkNotNull(fParser)
            return fParser.parseLine(line)
        }
    }

    /**
     * Iterates over data files.
     */
    inner class DataFileIterator(pos: POS?) : FileIterator2<Synset>(
        dataProvider.resolveContentType<Synset>(DataType.DATA, pos)!!
    ) {

        override fun parseLine(line: String): Synset {
            if (pOS == POS.ADJECTIVE) {
                checkNotNull(fParser)
                val synset = checkNotNull(fParser.parseLine(line))
                setHeadWord(synset)
                return synset
            } else {
                checkNotNull(fParser)
                return fParser.parseLine(line)
            }
        }
    }

    /**
     * Iterates over exception files.
     */
    inner class ExceptionFileIterator(pos: POS?) : FileIterator<IExceptionEntryProxy, IExceptionEntry>(
        dataProvider.resolveContentType<IExceptionEntryProxy>(DataType.EXCEPTION, pos)!!
    ) {

        override fun parseLine(line: String): IExceptionEntry {
            checkNotNull(fParser)
            val proxy = fParser.parseLine(line)
            return ExceptionEntry(proxy, pOS)
        }
    }
}
