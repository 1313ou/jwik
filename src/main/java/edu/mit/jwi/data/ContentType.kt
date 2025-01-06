/* ******************************************************************************
 * Java Wordnet Interface Library (JWI) v2.4.0
 * Copyright (c) 2007-2015 Mark A. Finlayson
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 4.0
 * International Public License, which means it may be freely used for all
 * purposes, as long as proper acknowledgment is made.  See the license file
 * included with this distribution for more details.
 *******************************************************************************/

package edu.mit.jwi.data;

import edu.mit.jwi.NonNull;
import edu.mit.jwi.Nullable;
import edu.mit.jwi.data.compare.*;
import edu.mit.jwi.item.*;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.*;

/**
 * A concrete implementation of the {@code IContentType} interface. This class
 * provides the content types necessary for Wordnet in the form of static
 * fields. It is not implemented as an {@code Enum} so that clients may add
 * their own content types by instantiating this class.
 *
 * @param <T> the type of object for the content type
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.0.0
 */
public class ContentType<T> implements IContentType<T>
{
    public static final ContentType<IIndexWord> INDEX_NOUN = new ContentType<>(ContentTypeKey.INDEX_NOUN, IndexLineComparator.getInstance());
    public static final ContentType<IIndexWord> INDEX_VERB = new ContentType<>(ContentTypeKey.INDEX_VERB, IndexLineComparator.getInstance());
    public static final ContentType<IIndexWord> INDEX_ADVERB = new ContentType<>(ContentTypeKey.INDEX_ADVERB, IndexLineComparator.getInstance());
    public static final ContentType<IIndexWord> INDEX_ADJECTIVE = new ContentType<>(ContentTypeKey.INDEX_ADJECTIVE, IndexLineComparator.getInstance());

    public static final ContentType<IIndexWord> WORD_NOUN = new ContentType<>(ContentTypeKey.WORD_NOUN, IndexLineComparator.getInstance());
    public static final ContentType<IIndexWord> WORD_VERB = new ContentType<>(ContentTypeKey.WORD_VERB, IndexLineComparator.getInstance());
    public static final ContentType<IIndexWord> WORD_ADVERB = new ContentType<>(ContentTypeKey.WORD_ADVERB, IndexLineComparator.getInstance());
    public static final ContentType<IIndexWord> WORD_ADJECTIVE = new ContentType<>(ContentTypeKey.WORD_ADJECTIVE, IndexLineComparator.getInstance());

    public static final ContentType<ISynset> DATA_NOUN = new ContentType<>(ContentTypeKey.DATA_NOUN, DataLineComparator.getInstance());
    public static final ContentType<ISynset> DATA_VERB = new ContentType<>(ContentTypeKey.DATA_VERB, DataLineComparator.getInstance());
    public static final ContentType<ISynset> DATA_ADVERB = new ContentType<>(ContentTypeKey.DATA_ADVERB, DataLineComparator.getInstance());
    public static final ContentType<ISynset> DATA_ADJECTIVE = new ContentType<>(ContentTypeKey.DATA_ADJECTIVE, DataLineComparator.getInstance());

    public static final ContentType<IExceptionEntryProxy> EXCEPTION_NOUN = new ContentType<>(ContentTypeKey.EXCEPTION_NOUN,
            ExceptionLineComparator.getInstance());
    public static final ContentType<IExceptionEntryProxy> EXCEPTION_VERB = new ContentType<>(ContentTypeKey.EXCEPTION_VERB,
            ExceptionLineComparator.getInstance());
    public static final ContentType<IExceptionEntryProxy> EXCEPTION_ADVERB = new ContentType<>(ContentTypeKey.EXCEPTION_ADVERB,
            ExceptionLineComparator.getInstance());
    public static final ContentType<IExceptionEntryProxy> EXCEPTION_ADJECTIVE = new ContentType<>(ContentTypeKey.EXCEPTION_ADJECTIVE,
            ExceptionLineComparator.getInstance());

    public static final ContentType<ISenseEntry> SENSE = new ContentType<>(ContentTypeKey.SENSE, SenseKeyLineComparator.getInstance());
    public static final ContentType<ISenseEntry[]> SENSES = new ContentType<>(ContentTypeKey.SENSES, SenseKeyLineComparator.getInstance());

    // fields set on construction
    @Nullable
    private final ContentTypeKey fKey;
    private final ILineComparator fComparator;
    @NonNull
    private final String fString;
    private final Charset fCharset;

    /**
     * Constructs a new ContentType
     *
     * @param key        content type key
     * @param comparator the line comparator for this content type; may be
     *                   <code>null</code> if the lines are not ordered
     * @since JWI 2.4.1
     */
    public ContentType(ContentTypeKey key, ILineComparator comparator)
    {
        this(key, comparator, null);
    }

    /**
     * Constructs a new ContentType
     *
     * @param key        content type key
     * @param comparator the line comparator for this content type; may be
     *                   <code>null</code> if the lines are not ordered
     * @param charset    the character set for this content type, may be
     *                   <code>null</code>
     * @since JWI 2.4.1
     */
    public ContentType(@Nullable ContentTypeKey key, ILineComparator comparator, Charset charset)
    {
        if (key == null)
        {
            throw new NullPointerException();
        }

        fKey = key;
        fComparator = comparator;
        fCharset = charset;

        if (fKey.getPOS() != null)
        {
            fString = "[ContentType: " + fKey.getDataType().toString() + "/" + fKey.getPOS() + "]";
        }
        else
        {
            fString = "[ContentType: " + fKey.getDataType().toString() + "]";
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.edu.mit.jwi.data.IContentType#getKey()
     */
    @NonNull
    public ContentTypeKey getKey()
    {
        assert fKey != null;
        return fKey;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.edu.mit.jwi.data.IContentType#getDataType()
     */
    @NonNull
    public IDataType<T> getDataType()
    {
        assert fKey != null;
        return fKey.getDataType();
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.edu.mit.jwi.item.IHasPOS#getPOS()
     */
    @Nullable
    public POS getPOS()
    {
        assert fKey != null;
        return fKey.getPOS();
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.edu.mit.jwi.data.IContentType#getLineComparator()
     */
    public ILineComparator getLineComparator()
    {
        return fComparator;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.edu.mit.jwi.data.IHasCharset#getCharset()
     */
    public Charset getCharset()
    {
        return fCharset;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @NonNull
    public String toString()
    {
        return fString;
    }

    // set of all content types implemented in this class
    @NonNull
    private static final Set<ContentType<?>> contentTypes;

    // initialization for static content type set
    static
    {
        // get all the fields containing ContentType
        Field[] fields = ContentType.class.getFields();
        List<Field> instanceFields = new ArrayList<>();
        for (Field field : fields)
        {
            if (field.getType() == ContentType.class)
            {
                instanceFields.add(field);
            }
        }

        // this is the backing set
        Set<ContentType<?>> hidden = new LinkedHashSet<>(instanceFields.size());

        // fill in the backing set
        ContentType<?> contentType;
        for (Field field : instanceFields)
        {
            try
            {
                contentType = (ContentType<?>) field.get(null);
                if (contentType == null)
                {
                    continue;
                }
                hidden.add(contentType);
            }
            catch (IllegalAccessException e)
            {
                // Ignore
            }
        }

        // make the value set unmodifiable
        contentTypes = Collections.unmodifiableSet(hidden);
    }

    /**
     * Emulates the Enum.values() function.
     *
     * @return all the static ContentType instances listed in the class, in the
     * order they are declared.
     * @since JWI 2.0.0
     */
    @NonNull
    public static Collection<ContentType<?>> values()
    {
        return contentTypes;
    }

    /**
     * Use this convenience method to retrieve the appropriate
     * {@code IIndexWord} content type for the specified POS.
     *
     * @param pos the part of speech for the content type, may not be
     *            <code>null</code>
     * @return the index content type for the specified part of speech
     * @throws NullPointerException if the specified part of speech is <code>null</code>
     * @since JWI 2.0.0
     */
    @NonNull
    public static IContentType<IIndexWord> getIndexContentType(@Nullable POS pos)
    {
        if (pos == null)
        {
            throw new NullPointerException();
        }
        switch (pos)
        {
            case NOUN:
                return INDEX_NOUN;
            case VERB:
                return INDEX_VERB;
            case ADVERB:
                return INDEX_ADVERB;
            case ADJECTIVE:
                return INDEX_ADJECTIVE;
        }
        throw new IllegalStateException("This should not happen.");
    }

    public static IContentType<IIndexWord> getWordContentType(@Nullable POS pos)
    {
        if (pos == null)
        {
            throw new NullPointerException();
        }
        switch (pos)
        {
            case NOUN:
                return WORD_NOUN;
            case VERB:
                return WORD_VERB;
            case ADVERB:
                return WORD_ADVERB;
            case ADJECTIVE:
                return WORD_ADJECTIVE;
        }
        throw new IllegalStateException("This should not happen.");
    }

    /**
     * Use this convenience method to retrieve the appropriate
     * {@code ISynset} content type for the specified POS.
     *
     * @param pos the part of speech for the content type, may not be
     *            <code>null</code>
     * @return the index content type for the specified part of speech
     * @throws NullPointerException if the specified part of speech is <code>null</code>
     * @since JWI 2.0.0
     */
    @NonNull
    public static IContentType<ISynset> getDataContentType(@Nullable POS pos)
    {
        if (pos == null)
        {
            throw new NullPointerException();
        }
        switch (pos)
        {
            case NOUN:
                return DATA_NOUN;
            case VERB:
                return DATA_VERB;
            case ADVERB:
                return DATA_ADVERB;
            case ADJECTIVE:
                return DATA_ADJECTIVE;
        }
        throw new IllegalStateException("How in the world did we get here?");
    }

    /**
     * Use this convenience method to retrieve the appropriate
     * {@code IExceptionEntryProxy} content type for the specified POS.
     *
     * @param pos the part of speech for the content type, may not be
     *            <code>null</code>
     * @return the index content type for the specified part of speech
     * @throws NullPointerException if the specified part of speech is <code>null</code>
     * @since JWI 2.0.0
     */
    @NonNull
    public static IContentType<IExceptionEntryProxy> getExceptionContentType(@Nullable POS pos)
    {
        if (pos == null)
        {
            throw new NullPointerException();
        }
        switch (pos)
        {
            case NOUN:
                return EXCEPTION_NOUN;
            case VERB:
                return EXCEPTION_VERB;
            case ADVERB:
                return EXCEPTION_ADVERB;
            case ADJECTIVE:
                return EXCEPTION_ADJECTIVE;
        }
        throw new IllegalStateException("Great Scott, there's been a rupture in the space-time continuum!");
    }
}
