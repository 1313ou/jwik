package edu.mit.jwi

import edu.mit.jwi.data.*
import edu.mit.jwi.data.IHasLifecycle.ObjectClosedException
import edu.mit.jwi.data.compare.ILineComparator
import edu.mit.jwi.data.parse.ILineParser
import edu.mit.jwi.item.*
import edu.mit.jwi.item.Synset.Companion.zeroFillOffset
import edu.mit.jwi.item.Word.Companion.checkLexicalId
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import java.util.Collections.emptyIterator
import kotlin.Throws

/**
 * A type of `IDictionary` which uses an instance of a `DataProvider` to obtain its data.

 * Basic implementation of the `IDictionary` interface. A path to the
 * Wordnet dictionary files must be provided. If no `IDataProvider` is
 * specified, it uses the default implementation provided with the distribution.
 *
 * Constructs a dictionary with a caller-specified `IDataProvider`.
 *
 * @param dataProvider data provider
 * @param config config bundle
 */
class DataSourceDictionary(
    val dataProvider: FileProvider,
    config: Config? = null,
) : IDictionary, IHasCharset {

    /**
     * Constructs a new dictionary that uses the Wordnet files located in a directory pointed to by the specified url
     *
     * @param wordnetDir an url pointing to a directory containing the wordnet data files on the filesystem
     * @param config     config parameters
     */
    @JvmOverloads
    constructor(wordnetDir: URL, config: Config? = null) : this(FileProvider(wordnetDir)) {
        configure(config)
    }

    /**
     * Constructs a new dictionary that uses the Wordnet files located in the specified directory
     *
     * @param wordnetDir a directory containing the wordnet data files on the filesystem
     * @param config     config parameters
     */
    @JvmOverloads
    constructor(wordnetDir: File, config: Config? = null) : this(FileProvider(wordnetDir)) {
        configure(config)
    }

    init {
        configure(config)
    }

    override val version: Version?
        get() {
            checkOpen()
            return dataProvider.version
        }

    // C O N F I G

    /**
     * Sets the character set associated with this dictionary.
     *
     * @param charset the possibly null character set to use when decoding files.
     */
    override var charset: Charset?
        get() = dataProvider.charset
        set(charset) {
            dataProvider.charset = charset
        }

    /**
     * Sets the comparator associated with this content type in this dictionary.
     * The comparator may be null in which case it is reset to defaults.
     *
     * @param contentTypeKey the content type for which the comparator is to be set.
     * @param comparator the possibly null comparator set to use when decoding files.
     * @throws IllegalStateException if the provider is currently open
     */
    private fun setComparator(contentTypeKey: ContentTypeKey, comparator: ILineComparator?) {
        dataProvider.setComparator(contentTypeKey, comparator)
    }

    /**
     * Sets pattern attached to content type key, that source files have to match to be selected.
     * This gives selection a first opportunity before falling back on standard data type selection.
     *
     * @param contentTypeKey the content type key for which the matcher is to be set.
     * @param pattern regexp pattern
     */
    private fun setSourceMatcher(contentTypeKey: ContentTypeKey, pattern: String?) {
        dataProvider.setSourceMatcher(contentTypeKey, pattern)
    }

    /**
     * Configure from config bundle
     */
    override fun configure(config: Config?) {
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

    // O P E N  /  C L O S E

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

    // L O O K  U P

    override fun getIndexWord(lemma: String, pos: POS): IndexWord? {
        checkOpen()
        return getIndexWord(IndexWordID(lemma, pos))
    }

    override fun getIndexWord(id: IndexWordID): IndexWord? {
        checkOpen()
        val content = dataProvider.resolveContentType<IndexWord>(DataType.INDEX, id.pOS)
        val file: IDataSource<*> = dataProvider.getSource<IndexWord>(content!!)!!
        val line = file.getLine(id.lemma)
        if (line == null) {
            return null
        }
        val dataType = content.dataType
        return dataType.parser.parseLine(line)
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

    private fun getWords(start: String, pos: POS, limit: Int, result: MutableSet<String>): Collection<String> {
        checkOpen()
        val content = dataProvider.resolveContentType<IndexWord>(DataType.WORD, pos)!!
        val dataType = content.dataType
        val parser: ILineParser<IndexWord> = dataType.parser
        val file: IDataSource<*> = dataProvider.getSource<IndexWord>(content)!!
        var found = false
        val lines = file.iterator(start)
        while (lines.hasNext()) {
            val line = lines.next()
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
            val synset = getSynset(SynsetID(entry.offset, entry.pOS))
            if (synset != null) {
                for (synonym in synset.words) {
                    if (synonym.senseKey == key) {
                        return synonym
                    }
                }
            }
        }

        // sometimes the sense.index file doesn't have the sense key entry
        // so try an alternate method of retrieving words by sense keys
        // We have to search the synonyms of the words returned from the index word search because some synsets have lemmas that differ only in case e.g., {earth, Earth} or {south, South},
        // and so separate entries are not found in the index file
        var word: Word? = null
        val indexWord = getIndexWord(key.lemma, key.pOS)
        if (indexWord != null) {
            var possibleWord: Word?
            for (wordID in indexWord.wordIDs) {
                possibleWord = getWord(wordID)
                if (possibleWord != null) {
                    val synset = possibleWord.synset
                    val words: List<Word> = synset.words
                    for (synonym in words) {
                        if (synonym.senseKey == key) {
                            word = synonym
                            val lemma = synonym.lemma
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

    override fun getSenseEntry(key: SenseKey): SenseEntry? {
        checkOpen()
        val content = dataProvider.resolveContentType<SenseEntry>(DataType.SENSE, null)
        val file = dataProvider.getSource<SenseEntry>(content!!)!!
        val line = file.getLine(key.toString())
        if (line == null) {
            return null
        }
        val dataType = content.dataType
        val parser = dataType.parser
        return parser.parseLine(line)
    }

    fun getSenseEntries(key: SenseKey): Array<SenseEntry>? {
        checkOpen()
        val content = dataProvider.resolveContentType<Array<SenseEntry>>(DataType.SENSES, null)
        val file = dataProvider.getSource<Array<SenseEntry>>(content!!)!!
        val line = file.getLine(key.toString())
        if (line == null) {
            return null
        }
        val dataType = content.dataType
        val parser = dataType.parser
        return parser.parseLine(line)
    }

    override fun getSynset(id: SynsetID): Synset? {
        checkOpen()
        val content = dataProvider.resolveContentType<Synset>(DataType.DATA, id.pOS)
        val file = dataProvider.getSource<Synset>(content!!)!!
        val zeroFilledOffset = zeroFillOffset(id.offset)
        val line = file.getLine(zeroFilledOffset)
        if (line == null) {
            return null
        }
        val dataType = content.dataType
        val parser = dataType.parser
        val result = parser.parseLine(line)
        setHeadWord(result)
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
            headSynset = getSynset(simID)!!
            // assume first 'similar' adjective head is the right one
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

    override fun getExceptionEntry(surfaceForm: String, pos: POS): ExceptionEntry? {
        return getExceptionEntry(ExceptionEntryID(surfaceForm, pos))
    }

    override fun getExceptionEntry(id: ExceptionEntryID): ExceptionEntry? {
        checkOpen()
        val content = dataProvider.resolveContentType<ExceptionEntryProxy>(DataType.EXCEPTION, id.pOS)
        val file = dataProvider.getSource<ExceptionEntryProxy>(content!!)
        // fix for bug 010
        if (file == null) {
            return null
        }
        val line = file.getLine(id.surfaceForm)
        if (line == null) {
            return null
        }
        val dataType = content.dataType
        val parser = dataType.parser
        val proxy = parser.parseLine(line)
        return ExceptionEntry(proxy, id.pOS)
    }

    override fun getIndexWordIterator(pos: POS): Iterator<IndexWord> {
        checkOpen()
        return IndexFileIterator(pos)
    }

    override fun getSynsetIterator(pos: POS): Iterator<Synset> {
        checkOpen()
        return DataFileIterator(pos)
    }

    override fun getExceptionEntryIterator(pos: POS): Iterator<ExceptionEntry> {
        checkOpen()
        return ExceptionFileIterator(pos)
    }

    override fun getSenseEntryIterator(): Iterator<SenseEntry> {
        checkOpen()
        return SenseEntryFileIterator()
    }

    /**
     * Abstract class used for iterating over line-based files.
     */
    abstract inner class FileIterator<T, N> @JvmOverloads constructor(content: ContentType<T>, startKey: String? = null) : Iterator<N>, IHasPOS {

        protected val source = dataProvider.getSource<T>(content)

        protected var iterator: Iterator<String>? = source?.iterator(startKey) ?: emptyIterator<String>() // Fix for Bug018

        protected val parser: ILineParser<T> = content.dataType.parser

        var currentLine: String? = null
            protected set

        override val pOS: POS
            get() {
                val contentType = source!!.contentType
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
     */
    abstract inner class FileIterator2<T> : FileIterator<T, T> {

        /**
         * Constructs a new file iterator with the specified content type.
         *
         * @param content content type
         */
        constructor(content: ContentType<T>) : super(content)

        /**
         * Constructs a new file iterator with the specified content type and start key.
         *
         * @param content  content type
         * @param startKey start key
         */
        constructor(content: ContentType<T>, startKey: String) : super(content, startKey)
    }

    /**
     * Iterates over index files.
     */
    inner class IndexFileIterator @JvmOverloads constructor(pos: POS, pattern: String = "") : FileIterator2<IndexWord>(
        dataProvider.resolveContentType<IndexWord>(DataType.INDEX, pos)!!,
        pattern
    ) {

        override fun parseLine(line: String): IndexWord {
            return parser.parseLine(line)
        }
    }

    /**
     * Iterates over the sense file.
     */
    inner class SenseEntryFileIterator : FileIterator2<SenseEntry>(
        dataProvider.resolveContentType<SenseEntry>(DataType.SENSE, null)!!
    ) {

        override fun parseLine(line: String): SenseEntry {
            return parser.parseLine(line)
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
                val synset = parser.parseLine(line)
                setHeadWord(synset)
                return synset
            } else {
                return parser.parseLine(line)
            }
        }
    }

    /**
     * Iterates over exception files.
     */
    inner class ExceptionFileIterator(pos: POS?) : FileIterator<ExceptionEntryProxy, ExceptionEntry>(
        dataProvider.resolveContentType<ExceptionEntryProxy>(DataType.EXCEPTION, pos)!!
    ) {

        override fun parseLine(line: String): ExceptionEntry {
            val proxy = parser.parseLine(line)
            return ExceptionEntry(proxy, pOS)
        }
    }
}
