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
import edu.mit.jwi.data.parse.*;
import edu.mit.jwi.item.*;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

/**
 * A concrete implementation of the {@code IDataType} interface. This class
 * provides the data types necessary for Wordnet in the form of static
 * fields. It is not implemented as an {@code Enum} so that clients may add
 * their own content types by instantiating this class.
 *
 * @param <T> the type of object for the content type
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.0.0
 */
public class DataType<T> implements IDataType<T>
{
    public static final DataType<IIndexWord> INDEX = new DataType<>("Index", true, IndexLineParser.getInstance(), "index", "idx");
    public static final DataType<IIndexWord> WORD = new DataType<>("Word", true, IndexLineParser.getInstance(), "index", "idx");
    public static final DataType<ISynset> DATA = new DataType<>("Data", true, DataLineParser.getInstance(), "data", "dat");
    public static final DataType<IExceptionEntryProxy> EXCEPTION = new DataType<>("Exception", false, ExceptionLineParser.getInstance(), "exception", "exc");
    public static final DataType<ISenseEntry> SENSE = new DataType<>("Sense", false, SenseLineParser.getInstance(), "sense");
    public static final DataType<ISenseEntry[]> SENSES = new DataType<>("Senses", false, SensesLineParser.getInstance(), "sense");

    // fields set on construction
    private final String name;
    private Set<String> hints;
    private final boolean hasVersion;
    @Nullable
    private final ILineParser<T> parser;

    /**
     * Constructs a new data type. This constructor takes the hints as a
     * varargs array.
     *
     * @param userFriendlyName a user-friendly name, for easy identification of this data
     *                         type; may be <code>null</code>
     * @param hasVersion       <code>true</code> if the comment header for this data type
     *                         usually contains a version number
     * @param parser           the line parser for transforming lines from this data type
     *                         into objects; may not be <code>null</code>
     * @param hints            a varargs array of resource name hints for identifying the
     *                         resource that contains the data. may be <code>null</code>, but
     *                         may not contain <code>null</code>
     * @throws NullPointerException if the specified parser is <code>null</code>
     * @since JWI 2.0.0
     */
    public DataType(String userFriendlyName, boolean hasVersion, ILineParser<T> parser, @Nullable String... hints)
    {
        this(userFriendlyName, hasVersion, parser, (hints == null) ? null : Arrays.asList(hints));
    }

    /**
     * Constructs a new data type. This constructor takes the hints as a
     * collection.
     *
     * @param userFriendlyName a user-friendly name, for easy identification of this data
     *                         type; may be <code>null</code>
     * @param hasVersion       <code>true</code> if the comment header for this data type
     *                         usually contains a version number
     * @param parser           the line parser for transforming lines from this data type
     *                         into objects; may not be <code>null</code>
     * @param hints            a collection of resource name hints for identifying the
     *                         resource that contains the data. May be <code>null</code>, but
     *                         may not contain <code>null</code>
     * @throws NullPointerException if the specified parser is <code>null</code>
     * @since JWI 2.0.0
     */
    public DataType(String userFriendlyName, boolean hasVersion, @Nullable ILineParser<T> parser, @Nullable Collection<String> hints)
    {
        if (parser == null)
        {
            throw new NullPointerException();
        }
        this.name = userFriendlyName;
        this.parser = parser;
        this.hasVersion = hasVersion;
        this.hints = (hints == null || hints.isEmpty()) ? Collections.emptySet() : Collections.unmodifiableSet(new HashSet<>(hints));
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.edu.mit.jwi.data.IDataType#hasVersion()
     */
    public boolean hasVersion()
    {
        return hasVersion;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.edu.mit.jwi.data.IDataType#getResourceNameHints()
     */
    public Set<String> getResourceNameHints()
    {
        return hints;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.edu.mit.jwi.data.IDataType#setResourceNameHints(java.util.Set)
     */
    public void setResourceNameHints(Set<String> hints)
    {
        this.hints = hints;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.edu.mit.jwi.data.IDataType#getParser()
     */
    @Nullable
    public ILineParser<T> getParser()
    {
        return parser;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @NonNull
    public String toString()
    {
        return name;
    }

    // set of all data types implemented in this class
    @Nullable
    private static Set<DataType<?>> dataTypes = null;

    /**
     * Emulates the Enum.values() function.
     *
     * @return all the static data type instances listed in the class, in the
     * order they are declared.
     * @since JWI 2.0.0
     */
    @NonNull
    public static Collection<DataType<?>> values()
    {
        if (dataTypes == null)
        {
            // get all the fields containing ContentType
            Field[] fields = DataType.class.getFields();
            List<Field> instanceFields = new ArrayList<>();
            for (Field field : fields)
            {
                if (field.getGenericType() == DataType.class)
                {
                    instanceFields.add(field);
                }
            }

            // this is the backing set
            Set<DataType<?>> hidden = new LinkedHashSet<>(instanceFields.size());

            // fill in the backing set
            DataType<?> dataType;
            for (Field field : instanceFields)
            {
                try
                {
                    dataType = (DataType<?>) field.get(null);
                    if (dataType != null)
                    {
                        hidden.add(dataType);
                    }
                }
                catch (IllegalAccessException e)
                {
                    // Ignore
                }
            }

            // make the value set unmodifiable
            dataTypes = Collections.unmodifiableSet(hidden);
        }
        return dataTypes;
    }

    /**
     * Finds the first file that satisfies the naming constraints of both
     * the data type and part of speech. Behaviour modified.
     *
     * @param dataType the data type whose resource name hints should be used, may
     *                 not be <code>null</code>
     * @param pos      the part of speech whose resource name hints should be used,
     *                 may be <code>null</code>
     * @param files    the files to be searched, may be empty but not <code>null</code>
     * @return the file that matches both the pos and type naming conventions,
     * or <code>null</code> if none is found.
     * @throws NullPointerException if the data type or file collection is <code>null</code>
     * @since JWI 2.2.0
     */
    @Nullable
    public static File find(@NonNull IDataType<?> dataType, @Nullable POS pos, @NonNull Collection<? extends File> files)
    {
        Set<String> typePatterns = dataType.getResourceNameHints();
        Set<String> posPatterns = (pos == null) ? Collections.emptySet() : pos.getResourceNameHints();
        if (typePatterns == null || typePatterns.isEmpty())
        {
            for (File file : files)
            {
                String name = file.getName().toLowerCase(); // added toLowerCase() as fix for Bug 017
                if (containsOneOf(name, posPatterns))
                {
                    return file;
                }
            }
        }
        else
        {
            for (String typePattern : typePatterns)
            {
                for (File file : files)
                {
                    String name = file.getName().toLowerCase(); // added toLowerCase() as fix for Bug 017
                    if (name.contains(typePattern) && containsOneOf(name, posPatterns))
                    {
                        return file;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Checks to see if one of the string patterns specified in the set of
     * strings is found in the specified target string. If the pattern set is
     * empty or null, returns <code>true</code>. If a pattern is found in the
     * target string, returns <code>true</code>. Otherwise, returns
     * <code>false</code>.
     *
     * @param target   the string to be searched
     * @param patterns the patterns to search for
     * @return <code>true</code> if the target contains one of the patterns;
     * <code>false</code> otherwise
     * @since JWI 2.2.0
     */
    public static boolean containsOneOf(@NonNull String target, @Nullable Set<String> patterns)
    {
        if (patterns == null || patterns.size() == 0)
        {
            return true;
        }
        for (String pattern : patterns)
        {
            if (target.contains(pattern))
            {
                return true;
            }
        }
        return false;
    }
}
