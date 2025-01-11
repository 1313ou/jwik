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
 * implementation is for files on disk, and uses a binary search algorithm to
 * find requested lines. It is appropriate for alphabetically-ordered Wordnet
 * files.
 *
 * Constructs a new binary search wordnet file, on the specified file with
 * the specified content type.
 *
 * @param file        the file which backs this wordnet file; may not be null
 * @param contentType the content type for this file; may not be null
 * @param <T> the type of object represented in this data resource
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.0.0
 */
class BinarySearchWordnetFile<T>(file: File, contentType: IContentType<T>) : WordnetFile<T>(file, contentType) {

    private val fComparator: Comparator<String>? = contentType.lineComparator

    private val bufferLock = Any()

    override fun getLine(key: String): String? {
        val buffer = getBuffer()

        synchronized(bufferLock) {
            var start = 0
            var midpoint: Int
            checkNotNull(buffer)
            var stop = buffer.limit()
            var cmp: Int
            var line: String?
            while (stop - start > 1) {
                // find the middle of the buffer
                midpoint = (start + stop) / 2
                buffer.position(midpoint)

                // back up to the beginning of the line
                rewindToLineStart(buffer)

                // read line
                checkNotNull(contentType)
                line = getLine(buffer, contentType.charset)

                // if we get a null, we've reached the end of the file
                cmp = if (line == null) 1 else fComparator!!.compare(line, key)

                // found our line
                if (cmp == 0) {
                    return line
                }

                if (cmp > 0) {
                    // too far forward
                    stop = midpoint
                } else {
                    // too far back
                    start = midpoint
                }
            }
        }
        return null
    }

    override fun makeIterator(buffer: ByteBuffer, key: String?): LineIterator {
        return BinarySearchLineIterator(buffer, key)
    }

    /**
     * Used to iterate over lines in a file. It is a look-ahead iterator. Does
     * not support the [.remove] method; if that method is called, it
     * will throw an [UnsupportedOperationException].
     *
     * Constructs a new line iterator over this buffer, starting at the
     * specified key.
     *
     * @param buffer the buffer over which the iterator should iterator; may
     * not be null
     * @param key    the key of the line to start at; may be null
     * @author Mark A. Finlayson
     * @version 2.4.0
     * @since JWI 2.0.0
     */
    inner class BinarySearchLineIterator(buffer: ByteBuffer, key: String?) : LineIterator(buffer) {

        private val bufferLock: Any = Any()

        init {
            init(key)
        }

        override fun findFirstLine(key: String) {
            synchronized(bufferLock) {
                var lastOffset = -1
                var start = 0
                var stop = itrBuffer.limit()
                while (start + 1 < stop) {
                    var midpoint: Int = (start + stop) / 2
                    itrBuffer.position(midpoint)
                    getLine(itrBuffer, contentType.charset)
                    var offset: Int = itrBuffer.position()
                    var line: String? = getLine(itrBuffer, contentType.charset)

                    // Fix for Bug009: If the line is null, we've reached
                    // the end of the file, so just advance to the first line
                    if (line == null) {
                        itrBuffer.position(itrBuffer.limit())
                        return
                    }

                    var compare: Int = fComparator!!.compare(line, key)
                    // if the key matches exactly, we know we have found
                    // the start of this pattern in the file
                    if (compare == 0) {
                        nextLine = line
                        return
                    } else if (compare > 0) {
                        stop = midpoint
                    } else {
                        start = midpoint
                    }
                    // if the key starts a line, remember it, because
                    // it may be the first occurrence
                    if (line.startsWith(key)) {
                        lastOffset = offset
                    }
                }

                // Getting here means that we didn't find an exact match
                // to the key, so we take the last line that started
                // with the pattern
                if (lastOffset > -1) {
                    itrBuffer.position(lastOffset)
                    nextLine = getLine(itrBuffer, contentType.charset)
                    return
                }

                // If we didn't have any lines that matched the pattern
                // then just advance to the first non-comment
                itrBuffer.position(itrBuffer.limit())
            }
        }
    }
}
