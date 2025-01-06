/* ******************************************************************************
 * Java Wordnet Interface Library (JWI) v2.4.0
 * Copyright (c) 2007-2015 Mark A. Finlayson
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 4.0
 * International Public License, which means it may be freely used for all
 * purposes, as long as proper acknowledgment is made.  See the license file
 * included with this distribution for more details.
 *******************************************************************************/

package edu.mit.jwi;

import edu.mit.jwi.data.*;
import edu.mit.jwi.data.compare.ILineComparator;
import edu.mit.jwi.data.parse.ILineParser;
import edu.mit.jwi.item.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Basic implementation of the {@code IDictionary} interface. A path to the
 * Wordnet dictionary files must be provided. If no {@code IDataProvider} is
 * specified, it uses the default implementation provided with the distribution.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.2.0
 */
public class DataSourceDictionary implements IDataSourceDictionary
{
    @NonNull
    private static <T> T requireNonNull(@Nullable T t)
    {
        if (t == null)
        {
            throw new NullPointerException();
        }
        return t;
    }

    @Nullable
    private final IDataProvider provider;

    /**
     * Constructs a dictionary with a caller-specified {@code IDataProvider}.
     *
     * @param provider data provider
     * @throws NullPointerException if the specified data provider is <code>null</code>
     */
    public DataSourceDictionary(@Nullable IDataProvider provider)
    {
        if (provider == null)
        {
            throw new NullPointerException();
        }
        this.provider = provider;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.mit.jwi.IDataSourceDictionary#getDataProvider()
     */
    @Nullable
    public IDataProvider getDataProvider()
    {
        return provider;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.edu.mit.jwi.item.IHasVersion#getVersion()
     */
    @Nullable
    public IVersion getVersion()
    {
        checkOpen();
        return provider.getVersion();
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.mit.jwi.IDictionary#open()
     */
    public boolean open() throws IOException
    {
        return provider.open();
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.mit.jwi.IDictionary#close()
     */
    public void close()
    {
        provider.close();
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.mit.jwi.IDictionary#isOpen()
     */
    public boolean isOpen()
    {
        return provider.isOpen();
    }

    /**
     * An internal method for assuring compliance with the dictionary interface
     * that says that methods will throw {@code ObjectClosedException}s if
     * the dictionary has not yet been opened.
     *
     * @throws ObjectClosedException if the dictionary is closed.
     */
    protected void checkOpen()
    {
        if (!isOpen())
        {
            throw new ObjectClosedException();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.edu.mit.jwi.data.IHasCharset#getCharset()
     */
    @Nullable
    public Charset getCharset()
    {
        return provider.getCharset();
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.mit.jwi.IDictionary#setCharset(java.nio.charset.Charset)
     */
    public void setCharset(Charset charset)
    {
        provider.setCharset(charset);
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.edu.mit.jwi.data.IDictionary#setComparator(edu.edu.mit.jwi.data.ContentTypeKey, edu.edu.mit.jwi.data.compare.ILineComparator)
     */
    public void setComparator(@NonNull ContentTypeKey contentTypeKey, ILineComparator comparator)
    {
        provider.setComparator(contentTypeKey, comparator);
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.edu.mit.jwi.data.IDictionary#setSourceMatcher(edu.mit.data ContentTypeKey, java.lang.String)
     */
    public void setSourceMatcher(@NonNull ContentTypeKey contentTypeKey, String pattern)
    {
        provider.setSourceMatcher(contentTypeKey, pattern);
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.mit.jwi.IDictionary#getIndexWord(java.lang.String,
     *      edu.edu.mit.jwi.item.POS)
     */
    @Nullable
    public IIndexWord getIndexWord(String lemma, POS pos)
    {
        checkOpen();
        return getIndexWord(new IndexWordID(lemma, pos));
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.mit.jwi.IDictionary#getIndexWord(edu.edu.mit.jwi.item.IIndexWordID)
     */
    @Nullable
    public IIndexWord getIndexWord(@NonNull IIndexWordID id)
    {
        checkOpen();
        IContentType<IIndexWord> content = provider.resolveContentType(DataType.INDEX, id.getPOS());
        IDataSource<?> file = provider.getSource(content);
        assert file != null;
        String line = file.getLine(id.getLemma());
        if (line == null)
        {
            return null;
        }
        assert content != null;
        IDataType<IIndexWord> dataType = content.getDataType();
        ILineParser<IIndexWord> parser = dataType.getParser();
        assert parser != null;
        return parser.parseLine(line);
    }

    @NonNull
    public Set<String> getWords(@NonNull String start, @Nullable POS pos, int limit)
    {
        checkOpen();
        Set<String> result = new TreeSet<>();
        if (pos != null)
        {
            getWords(start, pos, limit, result);
        }
        else
        {
            for (POS pos2 : POS.values())
            {
                getWords(start, pos2, limit,result);
            }
        }
        return result;
    }

    @NonNull
    protected Collection<String> getWords(@NonNull String start, @NonNull POS pos, int limit, @NonNull Set<String> result)
    {
        checkOpen();
        IContentType<IIndexWord> content = provider.resolveContentType(DataType.WORD, pos);
        assert content != null;
        IDataType<IIndexWord> dataType = content.getDataType();
        ILineParser<IIndexWord> parser = dataType.getParser();
        assert parser != null;

        IDataSource<?> file = provider.getSource(content);
        assert file != null;

        boolean found = false;
        Iterator<String> lines = file.iterator(start);
        while (lines.hasNext())
        {
            String line = lines.next();
            if (line != null)
            {
                boolean match = line.startsWith(start);
                if (match)
                {
                    IIndexWord index = parser.parseLine(line);
                    String lemma = index.getLemma();
                    result.add(lemma);
                    found = true;
                }
                else if (found)
                {
                    break;
                }
                if (limit > 0 && result.size() >= limit)
                {
                    break;
                }
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.mit.jwi.IDictionary#getWord(edu.edu.mit.jwi.item.IWordID)
     */
    @Nullable
    public IWord getWord(@NonNull IWordID id)
    {
        checkOpen();
        ISynsetID sid = id.getSynsetID();
        assert sid != null;
        ISynset synset = getSynset(sid);
        if (synset == null)
        {
            return null;
        }

        // One or the other of the WordID number or lemma may not exist,
        // depending on whence the word id came, so we have to check
        // them before trying.
        if (id.getWordNumber() > 0)
        {
            return synset.getWords().get(id.getWordNumber() - 1);
        }
        else if (id.getLemma() != null)
        {
            for (IWord word : synset.getWords())
            {
                String lemma = word.getLemma();
                assert lemma != null;
                if (lemma.equalsIgnoreCase(id.getLemma()))
                {
                    return word;
                }
            }
            return null;
        }
        else
        {
            throw new IllegalArgumentException("Not enough information in IWordID instance to retrieve word.");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.mit.jwi.IDictionary#getWord(edu.edu.mit.jwi.item.ISenseKey)
     */
    @Nullable
    public IWord getWord(@NonNull ISenseKey key)
    {
        checkOpen();

        // no need to cache result from the following calls as this will have been
        // done in the call to getSynset()
        ISenseEntry entry = getSenseEntry(key);
        if (entry != null)
        {
            ISynset synset = getSynset(new SynsetID(entry.getOffset(), entry.getPOS()));
            if (synset != null)
            {
                for (IWord synonym : synset.getWords())
                {
                    if (synonym.getSenseKey().equals(key))
                    {
                        return synonym;
                    }
                }
            }
        }

        IWord word = null;

        // sometimes the sense.index file doesn't have the sense key entry
        // so try an alternate method of retrieving words by sense keys
        // We have to search the synonyms of the words returned from the
        // index word search because some synsets have lemmas that differ only in case
        // e.g., {earth, Earth} or {south, South}, and so separate entries
        // are not found in the index file
        IIndexWord indexWord = getIndexWord(key.getLemma(), key.getPOS());
        if (indexWord != null)
        {
            IWord possibleWord;
            for (IWordID wordID : indexWord.getWordIDs())
            {
                possibleWord = getWord(wordID);
                if (possibleWord != null)
                {
                    ISynset synset = possibleWord.getSynset();
                    assert synset != null;
                    List<IWord> words = synset.getWords();
                    for (IWord synonym : words)
                    {
                        if (synonym.getSenseKey().equals(key))
                        {
                            word = synonym;
                            String lemma = synonym.getLemma();
                            assert lemma != null;
                            if (lemma.equals(key.getLemma()))
                            {
                                return synonym;
                            }
                        }
                    }
                }
            }
        }
        return word;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.mit.jwi.IDictionary#getSenseEntry(edu.edu.mit.jwi.item.ISenseKey)
     */
    @Nullable
    public ISenseEntry getSenseEntry(@NonNull ISenseKey key)
    {
        checkOpen();
        IContentType<ISenseEntry> content = provider.resolveContentType(DataType.SENSE, null);
        IDataSource<ISenseEntry> file = provider.getSource(content);
        assert file != null;
        String line = file.getLine(key.toString());
        if (line == null)
        {
            return null;
        }
        assert content != null;
        IDataType<ISenseEntry> dataType = content.getDataType();
        ILineParser<ISenseEntry> parser = dataType.getParser();
        assert parser != null;
        return parser.parseLine(line);
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.mit.jwi.IDictionary#getSenseEntries(edu.edu.mit.jwi.item.ISenseKey)
     */
    @Nullable
    public ISenseEntry[] getSenseEntries(@NonNull ISenseKey key)
    {
        checkOpen();
        IContentType<ISenseEntry[]> content = provider.resolveContentType(DataType.SENSES, null);
        IDataSource<ISenseEntry[]> file = provider.getSource(content);
        assert file != null;
        String line = file.getLine(key.toString());
        if (line == null)
        {
            return null;
        }
        assert content != null;
        IDataType<ISenseEntry[]> dataType = content.getDataType();
        ILineParser<ISenseEntry[]> parser = dataType.getParser();
        assert parser != null;
        return parser.parseLine(line);
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.mit.wordnet.core.dict.IDictionary#getSynset(edu.mit.wordnet.core.data.ISynsetID)
     */
    @Nullable
    public ISynset getSynset(@NonNull ISynsetID id)
    {
        checkOpen();
        IContentType<ISynset> content = provider.resolveContentType(DataType.DATA, id.getPOS());
        IDataSource<ISynset> file = provider.getSource(content);
        String zeroFilledOffset = Synset.zeroFillOffset(id.getOffset());
        assert file != null;
        String line = file.getLine(zeroFilledOffset);
        if (line == null)
        {
            return null;
        }
        assert content != null;
        IDataType<ISynset> dataType = content.getDataType();
        ILineParser<ISynset> parser = dataType.getParser();
        assert parser != null;
        ISynset result = parser.parseLine(line);
        if (result != null)
        {
            setHeadWord(result);
        }
        return result;
    }

    /**
     * This method sets the head word on the specified synset by searching in
     * the dictionary to find the head of its cluster. We will assume the head
     * is the first adjective head synset related by an '&amp;' pointer (SIMILAR_TO)
     * to this synset.
     *
     * @param synset synset
     */
    protected void setHeadWord(@NonNull ISynset synset)
    {
        // head words are only needed for adjective satellites
        if (!synset.isAdjectiveSatellite())
        {
            return;
        }

        // go find the head word
        ISynset headSynset;
        IWord headWord = null;
        List<ISynsetID> related = synset.getRelatedSynsets(Pointer.SIMILAR_TO);
        assert related != null;
        for (ISynsetID simID : related)
        {
            headSynset = getSynset(simID);
            // assume first 'similar' adjective head is the right one
            assert headSynset != null;
            if (headSynset.isAdjectiveHead())
            {
                headWord = headSynset.getWords().get(0);
                break;
            }
        }
        if (headWord == null)
        {
            return;
        }

        // set head word, if we found it
        String headLemma = headWord.getLemma();

        // version 1.6 of Wordnet adds the adjective marker symbol
        // on the end of the head word lemma
        IVersion ver = getVersion();
        boolean isVer16 = (ver != null) && (ver.getMajorVersion() == 1 && ver.getMinorVersion() == 6);
        if (isVer16 && headWord.getAdjectiveMarker() != null)
        {
            headLemma += headWord.getAdjectiveMarker().getSymbol();
        }

        // set the head word for each word
        for (IWord word : synset.getWords())
        {
            if (word.getSenseKey().needsHeadSet())
            {
                word.getSenseKey().setHead(headLemma, headWord.getLexicalID());
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.mit.jwi.IDictionary#getExceptionEntry(java.lang.String,
     *      edu.edu.mit.jwi.item.POS)
     */
    @Nullable
    public IExceptionEntry getExceptionEntry(String surfaceForm, POS pos)
    {
        return getExceptionEntry(new ExceptionEntryID(surfaceForm, pos));
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.mit.jwi.IDictionary#getExceptionEntry(edu.edu.mit.jwi.item.IExceptionEntryID)
     */
    @Nullable
    public IExceptionEntry getExceptionEntry(@NonNull IExceptionEntryID id)
    {
        checkOpen();
        IContentType<IExceptionEntryProxy> content = provider.resolveContentType(DataType.EXCEPTION, id.getPOS());
        IDataSource<IExceptionEntryProxy> file = provider.getSource(content);
        // fix for bug 010
        if (file == null)
        {
            return null;
        }
        String line = file.getLine(id.getSurfaceForm());
        if (line == null)
        {
            return null;
        }
        assert content != null;
        IDataType<IExceptionEntryProxy> dataType = content.getDataType();
        ILineParser<IExceptionEntryProxy> parser = dataType.getParser();
        assert parser != null;
        IExceptionEntryProxy proxy = parser.parseLine(line);
        if (proxy == null)
        {
            return null;
        }
        return new ExceptionEntry(proxy, id.getPOS());
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.mit.jwi.IDictionary#getIndexWordIterator(edu.edu.mit.jwi.item.POS)
     */
    @NonNull
    public Iterator<IIndexWord> getIndexWordIterator(POS pos)
    {
        checkOpen();
        return new IndexFileIterator(pos);
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.mit.jwi.IDictionary#getSynsetIterator(edu.edu.mit.jwi.item.POS)
     */
    @NonNull
    public Iterator<ISynset> getSynsetIterator(POS pos)
    {
        checkOpen();
        return new DataFileIterator(pos);
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.mit.jwi.IDictionary#getExceptionEntryIterator(edu.edu.mit.jwi.item.POS)
     */
    @NonNull
    public Iterator<IExceptionEntry> getExceptionEntryIterator(POS pos)
    {
        checkOpen();
        return new ExceptionFileIterator(pos);
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.mit.jwi.IDictionary#getSenseEntryIterator()
     */
    @NonNull
    public Iterator<ISenseEntry> getSenseEntryIterator()
    {
        checkOpen();
        return new SenseEntryFileIterator();
    }

    /**
     * Abstract class used for iterating over line-based files.
     */
    public abstract class FileIterator<T, N> implements Iterator<N>, IHasPOS
    {
        @Nullable
        protected final IDataSource<T> fFile;
        @NonNull
        protected final Iterator<String> iterator;
        @Nullable
        protected final ILineParser<T> fParser;
        protected String currentLine;

        public FileIterator(@NonNull IContentType<T> content)
        {
            this(content, null);
        }

        public FileIterator(@NonNull IContentType<T> content, String startKey)
        {
            assert provider != null;
            this.fFile = provider.getSource(content);
            IDataType<T> dataType = content.getDataType();
            this.fParser = dataType.getParser();
            if (fFile == null)
            {
                // Fix for Bug018
                this.iterator = Collections.emptyIterator();
            }
            else
            {
                this.iterator = fFile.iterator(startKey);
            }
        }

        /**
         * Returns the current line.
         *
         * @return the current line
         * @since JWI 2.2.0
         */
        public String getCurrentLine()
        {
            return currentLine;
        }

        /*
         * (non-Javadoc)
         *
         * @see edu.mit.wordnet.data.IHasPartOfSpeech#getPartOfSpeech()
         */
        @Nullable
        public POS getPOS()
        {
            assert fFile != null;
            IContentType<T> contentType = fFile.getContentType();
            assert contentType != null;
            return contentType.getPOS();
        }

        /*
         * (non-Javadoc)
         *
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext()
        {
            return iterator.hasNext();
        }

        /*
         * (non-Javadoc)
         *
         * @see java.util.Iterator#next()
         */
        @Nullable
        public N next()
        {
            currentLine = iterator.next();
            return parseLine(currentLine);
        }

        /*
         * (non-Javadoc)
         *
         * @see java.util.Iterator#remove()
         */
        public void remove()
        {
            iterator.remove();
        }

        /**
         * Parses the line using a parser provided at construction time
         *
         * @param line line
         * @return parsed object
         */
        @Nullable
        public abstract N parseLine(String line);
    }

    /**
     * A file iterator where the data type returned by the iterator is the same
     * as that returned by the backing data source.
     *
     * @author Mark A. Finlayson
     * @since JWI 2.1.5
     */
    public abstract class FileIterator2<T> extends FileIterator<T, T>
    {
        /**
         * Constructs a new file iterator with the specified content type.
         *
         * @param content content type
         * @since JWI 2.1.5
         */
        public FileIterator2(@NonNull IContentType<T> content)
        {
            super(content);
        }

        /**
         * Constructs a new file iterator with the specified content type and start key.
         *
         * @param content  content type
         * @param startKey start key
         * @since JWI 2.1.5
         */
        public FileIterator2(@NonNull IContentType<T> content, String startKey)
        {
            super(content, startKey);
        }
    }

    /**
     * Iterates over index files.
     */
    public class IndexFileIterator extends FileIterator2<IIndexWord>
    {
        public IndexFileIterator(POS pos)
        {
            this(pos, "");
        }

        public IndexFileIterator(POS pos, String pattern)
        {
            super(requireNonNull(requireNonNull(provider).resolveContentType(DataType.INDEX, pos)), pattern);
        }

        /*
         * (non-Javadoc)
         *
         * @see edu.mit.wordnet.core.base.dict.Dictionary.FileIterator#parseLine(java.lang.String)
         */
        public IIndexWord parseLine(String line)
        {
            assert fParser != null;
            return fParser.parseLine(line);
        }
    }

    /**
     * Iterates over the sense file.
     */
    public class SenseEntryFileIterator extends FileIterator2<ISenseEntry>
    {
        public SenseEntryFileIterator()
        {
            super(requireNonNull(requireNonNull(provider).resolveContentType(DataType.SENSE, null)));
        }

        /*
         * (non-Javadoc)
         *
         * @see edu.mit.wordnet.core.base.dict.Dictionary.FileIterator#parseLine(java.lang.String)
         */
        public ISenseEntry parseLine(String line)
        {
            assert fParser != null;
            return fParser.parseLine(line);
        }
    }

    /**
     * Iterates over data files.
     */
    public class DataFileIterator extends FileIterator2<ISynset>
    {
        public DataFileIterator(POS pos)
        {
            super(requireNonNull(requireNonNull(provider).resolveContentType(DataType.DATA, pos)));
        }

        /*
         * (non-Javadoc)
         *
         * @see edu.mit.wordnet.core.base.dict.Dictionary.FileIterator#parseLine(java.lang.String)
         */
        public ISynset parseLine(String line)
        {
            if (getPOS() == POS.ADJECTIVE)
            {
                assert fParser != null;
                ISynset synset = fParser.parseLine(line);
                assert synset != null;
                setHeadWord(synset);
                return synset;
            }
            else
            {
                assert fParser != null;
                return fParser.parseLine(line);
            }
        }
    }

    /**
     * Iterates over exception files.
     */
    public class ExceptionFileIterator extends FileIterator<IExceptionEntryProxy, IExceptionEntry>
    {
        public ExceptionFileIterator(POS pos)
        {
            super(requireNonNull(requireNonNull(provider).resolveContentType(DataType.EXCEPTION, pos)));
        }

        /*
         * (non-Javadoc)
         *
         * @see edu.mit.wordnet.dict.Dictionary.FileIterator#parseLine(java.lang.String)
         */
        @Nullable
        public IExceptionEntry parseLine(String line)
        {
            assert fParser != null;
            IExceptionEntryProxy proxy = fParser.parseLine(line);
            return (proxy == null) ? null : new ExceptionEntry(proxy, getPOS());
        }
    }
}
