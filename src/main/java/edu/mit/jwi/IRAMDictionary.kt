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

import edu.mit.jwi.data.ILoadPolicy
import edu.mit.jwi.data.ILoadable
import java.io.*
import java.net.URL

/**
 * Interface that governs dictionaries that can be completely loaded into memory.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.2.0
 */
interface IRAMDictionary : IDictionary, ILoadPolicy, ILoadable {

    /**
     * Exports the in-memory contents of the data to the specified output stream.
     * This method flushes and closes the output stream when it is done writing
     * the data.
     *
     * @param out the output stream to which the in-memory data will be written;
     * may not be `null`
     * @throws IOException           if there is a problem writing the in-memory data to the
     * output stream.
     * @throws IllegalStateException if the dictionary has not been loaded into memory
     * @throws NullPointerException  if the output stream is `null`
     * @since JWI 2.4.0
     */
    @Throws(IOException::class)
    fun export(out: OutputStream)

    /**
     * An input stream factory is used by certain constructors of the
     * [RAMDictionary] class to provide source data to load the dictionary
     * into memory from a stream. Using this interface allows the dictionary to
     * be closed and reopened again.  Therefore, the expectation is that
     * the [.makeInputStream] method may be called multiple times
     * without throwing an exception.
     *
     * @author Mark A. Finlayson
     * @version 2.4.0
     * @since JWI 2.4.0
     */
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
     * @author Mark A. Finlayson
     * @version 2.4.0
     * @since JWI 2.4.0
     */
    class FileInputStreamFactory(file: File) : IInputStreamFactory {

        private val file: File

        /**
         * Creates a FileInputStreamFactory that uses the specified file.
         *
         * @param file the file from which the input streams should be created;
         * may not be `null`
         * @throws NullPointerException if the specified file is `null`
         * @since JWI 2.4.0
         */
        init {
            if (file == null) {
                throw NullPointerException()
            }
            this.file = file
        }

        @Throws(IOException::class)
        override fun makeInputStream(): InputStream {
            return FileInputStream(file)
        }
    }

    /**
     * Default implementation of the [IInputStreamFactory] interface which
     * creates an input stream from a specified URL.
     *
     * @author Mark A. Finlayson
     * @version 2.4.0
     * @since JWI 2.4.0
     */
    class URLInputStreamFactory(url: URL) : IInputStreamFactory {

        val url: URL

        /**
         * Creates a URLInputStreamFactory that uses the specified url.
         *
         * @param url the url from which the input streams should be created;
         * may not be `null`
         * @throws NullPointerException if the specified url is `null`
         * @since JWI 2.4.0
         */
        init {
            if (url == null) {
                throw NullPointerException()
            }
            this.url = url
        }

        @Throws(IOException::class)
        override fun makeInputStream(): InputStream? {
            return url.openStream()
        }
    }
}
