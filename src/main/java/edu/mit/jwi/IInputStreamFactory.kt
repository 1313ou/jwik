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

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL

interface IInputStreamFactory {

    /**
     * Returns a new input stream from this factory.
     *
     * @return a new, unused input stream from this factory.
     * @throws IOException io exception
     * @since JWI 2.4.0
     */
    @Throws(IOException::class)
    fun makeInputStream(): InputStream?
}

/**
 * Default implementation of the [IInputStreamFactory] interface which
 * creates an input stream from a specified File object.
 *
 * Creates a FileInputStreamFactory that uses the specified file.
 *
 * @param file the file from which the input streams should be created;
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.4.0
 */
class FileInputStreamFactory(private val file: File) : IInputStreamFactory {

    @Throws(IOException::class)
    override fun makeInputStream(): InputStream {
        return FileInputStream(file)
    }
}

/**
 * Default implementation of the [IInputStreamFactory] interface which
 * creates an input stream from a specified URL.
 *
 * Creates a URLInputStreamFactory that uses the specified url.
 *
 * @param url the url from which the input streams should be created;
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.4.0
 */
class URLInputStreamFactory(val url: URL) : IInputStreamFactory {

    @Throws(IOException::class)
    override fun makeInputStream(): InputStream? {
        return url.openStream()
    }
}
