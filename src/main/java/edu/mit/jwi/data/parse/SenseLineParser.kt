/* ******************************************************************************
 * Java Wordnet Interface Library (JWI) v2.4.0
 * Copyright (c) 2007-2015 Mark A. Finlayson
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 4.0
 * International Public License, which means it may be freely used for all
 * purposes, as long as proper acknowledgment is made.  See the license file
 * included with this distribution for more details.
 *******************************************************************************/

package edu.mit.jwi.data.parse;

import edu.mit.jwi.NonNull;
import edu.mit.jwi.Nullable;
import edu.mit.jwi.item.ISenseEntry;
import edu.mit.jwi.item.ISenseKey;
import edu.mit.jwi.item.SenseEntry;

import java.util.StringTokenizer;

/**
 * Parser for Wordnet sense index files (e.g., <code>index.sense</code> or
 * <code>sense.index</code>). It produces an {@code ISenseEntry} object.
 * <p>
 * This class follows a singleton design pattern, and is not intended to be
 * instantiated directly; rather, call the {@link #getInstance()} method to get
 * the singleton instance.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.1.0
 */
public class SenseLineParser implements ILineParser<ISenseEntry>
{
    // singleton instance
    private static SenseLineParser instance;

    /**
     * Returns the singleton instance of this class, instantiating it if
     * necessary. The singleton instance will not be <code>null</code>.
     *
     * @return the non-<code>null</code> singleton instance of this class,
     * instantiating it if necessary.
     * @since JWI 2.1.0
     */
    public static SenseLineParser getInstance()
    {
        if (instance == null)
        {
            instance = new SenseLineParser();
        }
        return instance;
    }

    // instance fields
    @Nullable
    protected final ILineParser<ISenseKey> keyParser;

    /**
     * This constructor is marked protected so that the class may be
     * sub-classed, but not directly instantiated. Obtain instances of this
     * class via the static {@link #getInstance()} method.
     *
     * @since JWI 2.1.0
     */
    protected SenseLineParser()
    {
        this(SenseKeyParser.getInstance());
    }

    /**
     * This constructor is marked protected so that the class may be
     * sub-classed, but not directly instantiated. Obtain instances of this
     * class via the static {@link #getInstance()} method.
     *
     * @param keyParser the sense key parser this sense line parser should use
     * @throws NullPointerException if the specified key parser is <code>null</code>
     * @since JWI 2.2.0
     */
    protected SenseLineParser(@Nullable ILineParser<ISenseKey> keyParser)
    {
        if (keyParser == null)
        {
            throw new NullPointerException();
        }
        this.keyParser = keyParser;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.edu.mit.jwi.data.parse.ILineParser#parseLine(java.lang.String)
     */
    @NonNull
    public ISenseEntry parseLine(@Nullable String line)
    {
        if (line == null)
        {
            throw new NullPointerException();
        }

        try
        {
            // get sense key
            int end = line.indexOf(' ');
            String keyStr = line.substring(0, end);
            assert keyParser != null;
            ISenseKey senseKey = keyParser.parseLine(keyStr);

            // get sense entry
            String tail = line.substring(end + 1);
            StringTokenizer tokenizer = new StringTokenizer(tail);
            return parseSenseEntry(tokenizer, senseKey);
        }
        catch (Exception e)
        {
            throw new MisformattedLineException(line, e);
        }
    }

    @NonNull
    protected static SenseEntry parseSenseEntry(@NonNull StringTokenizer tokenizer, ISenseKey senseKey)
    {
        // get offset
        int synsetOffset = Integer.parseInt(tokenizer.nextToken());

        // get sense number
        int senseNumber = Integer.parseInt(tokenizer.nextToken());

        // get tag cnt
        int tagCnt = Integer.parseInt(tokenizer.nextToken());

        return new SenseEntry(senseKey, synsetOffset, senseNumber, tagCnt);
    }
}
