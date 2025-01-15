package edu.mit.jwi.data

import java.io.File
import java.nio.ByteBuffer

/**
 * A wordnet file data source.
 * This particular implementation is for files on disk, and directly accesses the appropriate byte offset in the file to find requested lines.
 * It is appropriate for Wordnet data files.
 *
 * Constructs a new direct access wordnet file, on the specified file with the specified content type.
 *
 * @param file the file which backs this wordnet file
 * @param contentType the content type for this file
 * @param <T> the type of object represented in this data resource
 */
class DirectAccessWordnetFile<T>(file: File, contentType: ContentType<T>) : WordnetFile<T>(file, contentType) {

    private val bufferLock = Any()

    override fun getLine(key: String): String? {
        val buffer = getBuffer()
        synchronized(bufferLock) {
            try {
                val byteOffset = key.toInt()
                if (buffer.limit() <= byteOffset) {
                    return null
                }
                buffer.position(byteOffset)
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
     * Constructs a new line iterator over this buffer, starting at the specified key.
     *
     * @param buffer the buffer over which the iterator should iterator
     * @param key the key of the line to start at
     */
    private inner class DirectLineIterator(buffer: ByteBuffer, key: String?) : LineIterator(buffer) {

        private val bufferLock = Any()

        init {
            startAt(key)
        }

        override fun findFirstLine(key: String) {
            synchronized(bufferLock) {
                try {
                    val byteOffset = key.toInt()
                    if (itrBuffer.limit() <= byteOffset) {
                        return
                    }
                    itrBuffer.position(byteOffset)
                    nextLine = getLine(itrBuffer, contentType.charset)
                } catch (_: NumberFormatException) {
                    // ignore
                }
            }
        }
    }
}
