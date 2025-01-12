/* ******************************************************************************
 * Java Wordnet Interface Library (JWI) v2.4.0
 * Copyright (c) 2007-2015 Mark A. Finlayson
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 4.0
 * International Public License, which means it may be freely used for all
 * purposes, as long as proper acknowledgment is made.  See the license file
 * included with this distribution for more details.
 *******************************************************************************/
package edu.mit.jwi.data

import java.io.File
import java.nio.ByteBuffer

/**
 * Concrete implementation of a wordnet file data source. This particular
 * implementation is for files on disk, and directly accesses the appropriate
 * byte offset in the file to find requested lines. It is appropriate for
 * Wordnet data files.
 *
 * Constructs a new direct access wordnet file, on the specified file with
 * the specified content type.
 *
 * @param file        the file which backs this wordnet file
 * @param contentType the content type for this file
 *
 * @param <T> the type of object represented in this data resource
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.0.0
</T> */
class DirectAccessWordnetFile<T>(file: File, contentType: ContentType<T>) : WordnetFile<T>(file, contentType) {

    private val bufferLock = Any()

    override fun getLine(key: String): String? {
        val buffer = getBuffer()
        synchronized(bufferLock) {
            try {
                val byteOffset = key.toInt()
                checkNotNull(buffer)
                if (buffer.limit() <= byteOffset) {
                    return null
                }
                buffer.position(byteOffset)
                checkNotNull(contentType)
                val line = getLine(buffer, contentType.charset)
                return if (line != null && line.startsWith(key)) line else null
            } catch (_: NumberFormatException) {
                return null
            }
        }
    }

    override fun makeIterator(buffer: ByteBuffer, key: String?): LineIterator {
        return DirectLineIterator(buffer, key)
    }

    /**
     * Used to iterate over lines in a file. It is a look-ahead iterator.
     *
     * @author Mark A. Finlayson
     * @version 2.4.0
     * @since JWI 2.0.0
     */
    private inner class DirectLineIterator(buffer: ByteBuffer, key: String?) : LineIterator(buffer) {

        private val bufferLock = Any()

        /**
         * Constructs a new line iterator over this buffer, starting at the
         * specified key.
         *
         * @param buffer the buffer over which the iterator should iterator; may
         * not be null
         * @param key    the key of the line to start at; may be null
         * @throws NullPointerException if the specified buffer is null
         * @since JWI 2.0.0
         */
        init {
            init(key)
        }

        override fun findFirstLine(key: String) {
            synchronized(bufferLock) {
                try {
                    val byteOffset = key.toInt()
                    if (itrBuffer.limit() <= byteOffset) {
                        return
                    }
                    itrBuffer.position(byteOffset)
                    checkNotNull(contentType)
                    nextLine = getLine(itrBuffer, contentType.charset)
                } catch (_: NumberFormatException) {
                    // ignore
                }
            }
        }
    }
}
