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

import edu.mit.jwi.data.IHasLifecycle.ObjectClosedException
import edu.mit.jwi.data.compare.ICommentDetector
import edu.mit.jwi.item.Version
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.Throws

/**
 * Abstract superclass of wordnet data file objects. Provides all the
 * infrastructure required to access the files, except for the construction of
 * iterators and the actual implementation of the [.getLine]
 * method.
 *
 * While this object is implemented to provider load/unload capabilities (i.e.,
 * it allows the whole wordnet file to be loaded into memory, rather than read
 * from disk), this does not provide much of a performance boost. In tests, the
 * time to parsing a line of data into a data object dominates the time required
 * to read the data from disk (for a reasonable modern hard drive).
 *
 * Constructs an instance of this class backed by the specified java
 * `File` object, with the specified content type. No effort is made
 * to ensure that the data in the specified file is actually formatted in
 * the proper manner for the line parser associated with the content type's
 * data type. If these are mismatched, this will result in
 * `MisformattedLineExceptions` in later calls.
 *
 * @param file        the file which backs this wordnet file; may not be 'null'
 * @param contentType the content type for this file; may not be null

 * @param <T> the type of the objects represented in this file
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
abstract class WordnetFile<T>(
    /**
     * The file which backs this object.
     */
    val file: File,
    override val contentType: IContentType<T>,
) : ILoadableDataSource<T> {

    override val name: String = file.getName()

    private val detector: ICommentDetector? = contentType.lineComparator!!.commentDetector

    // loading locks and status flag
    // the flag is marked transient to avoid different values in different threads
    @Transient
    final override var isLoaded: Boolean = false

    private val lifecycleLock: Lock = ReentrantLock()

    private val loadingLock: Lock = ReentrantLock()

    private var channel: FileChannel? = null

    private var buffer: ByteBuffer? = null

    /**
     * Returns the buffer which backs this object.
     *
     * @return the buffer which backs this object
     * @throws ObjectClosedException if the object is closed
     * @since JWI 2.2.0
     */
    fun getBuffer(): ByteBuffer {
        if (!isOpen) {
            throw ObjectClosedException()
        }
        return buffer!!
    }

    /*
    * (non-Javadoc)
    *
    * @see edu.edu.mit.jwi.data.IHasLifecycle#open()
    */
    @Throws(IOException::class)
    override fun open(): Boolean {
        try {
            lifecycleLock.lock()
            if (isOpen) {
                return true
            }
            val raFile = RandomAccessFile(file, "r")
            channel = raFile.channel
            buffer = channel!!.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
            return true
        } finally {
            lifecycleLock.unlock()
        }
    }

    override val isOpen: Boolean
        get() {
            try {
                lifecycleLock.lock()
                return buffer != null
            } finally {
                lifecycleLock.unlock()
            }
        }

    override fun close() {
        try {
            lifecycleLock.lock()
            version = null
            buffer = null
            isLoaded = false
            if (channel != null) {
                try {
                    channel!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            channel = null
        } finally {
            lifecycleLock.unlock()
        }
    }

    /*
    * (non-Javadoc)
    *
    * @see edu.edu.mit.jwi.data.ILoadable#load()
    */
    override fun load() {
        load(false)
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.edu.mit.jwi.data.ILoadable#load(boolean)
     */
    override fun load(block: Boolean) {
        try {
            loadingLock.lock()
            val len = file.length().toInt()
            checkNotNull(buffer)
            val buf = buffer!!.asReadOnlyBuffer()
            buf.clear()
            val data = ByteArray(len)
            buf.get(data, 0, len)

            try {
                lifecycleLock.lock()
                if (channel != null) {
                    try {
                        channel!!.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    channel = null
                }
                if (buffer != null) {
                    buffer = ByteBuffer.wrap(data)
                    isLoaded = true
                }
            } finally {
                lifecycleLock.unlock()
            }
        } finally {
            loadingLock.unlock()
        }
    }

    /**
     * Returns the wordnet version associated with this object, or null if the
     * version cannot be determined.
     *
     * @return the wordnet version associated with this object, or null if the
     * version cannot be determined
     * @throws ObjectClosedException if the object is closed when this method is called
     * @see edu.mit.jwi.item.IHasVersion.version
     */
    override var version: Version? = null
        get() {
            if (!isOpen) {
                throw ObjectClosedException()
            }
            if (field == null) {
                val v = Version.extractVersion(contentType, buffer!!.asReadOnlyBuffer())
                if (v == null) {
                    field = Version.NO_VERSION
                }
            }
            return if (field === Version.NO_VERSION) null else field
        }

    override fun iterator(): LineIterator {
        if (!isOpen) {
            throw ObjectClosedException()
        }
        return makeIterator(getBuffer(), null)
    }

    override fun iterator(key: String?): LineIterator {
        if (!isOpen) {
            throw ObjectClosedException()
        }
        return makeIterator(getBuffer(), key)
    }

    /**
     * Constructs an iterator that can be used to iterate over the specified
     * [ByteBuffer], starting from the specified key.
     *
     * @param buffer the buffer over which the iterator will iterate, should not be
     * null
     * @param key    the key at which the iterator should begin, should not be
     * null
     * @return an iterator that can be used to iterate over the lines of the
     * [ByteBuffer]
     * @since JWI 2.2.0
     */
    abstract fun makeIterator(buffer: ByteBuffer, key: String?): LineIterator

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        checkNotNull(contentType)
        result = prime * result + contentType.hashCode()
        result = prime * result + file.hashCode()
        return result
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as WordnetFile<*>
        checkNotNull(contentType)
        if (contentType != other.contentType) {
            return false
        }
        return file == other.file
    }

    /**
     * Used to iterate over lines in a file. It is a look-ahead iterator. This
     * iterator does not support the remove method; if that method is called, it
     * throws an [UnsupportedOperationException].
     *
     * @author Mark A. Finlayson
     * @version 2.4.0
     * @since JWI 1.0
     */
    abstract inner class LineIterator(buffer: ByteBuffer) : Iterator<String> {

        protected val parentBuffer: ByteBuffer = buffer

        @JvmField
        protected var itrBuffer: ByteBuffer
        /**
         * Returns the line currently stored as the 'next' line, if any. Is a
         * pure getter; does not increment the iterator.
         *
         * @return the next line that will be parsed and returned by this
         * iterator, or null if none
         * @since JWI 2.2.0
         */
        var nextLine: String? = null
            protected set

        /**
         * Constructs a new line iterator over this buffer, starting at the
         * specified key.
         *
         * @param buffer the buffer over which the iterator should iterator; may
         * not be null
         * @throws NullPointerException if the specified buffer is null
         * @since JWI 1.0
         */
        init {
            itrBuffer = buffer.asReadOnlyBuffer()
            itrBuffer.clear()
        }

        /**
         * Start at the specified key.
         *
         * @param key0 the key of the line to start at; may be null
         */
        fun init(key0: String?) {
            var key = key0?.trim { it <= ' ' }
            if (key == null || key.isEmpty()) {
                advance()
            } else {
                findFirstLine(key)
            }
        }

        /**
         * Advances the iterator the first line the iterator should return,
         * based on the specified key. If the key is not found in the file, it
         * will advance the iterator past all lines.
         *
         * @param key the key indexed the first line to be returned by the
         * iterator
         * @since JWI 1.0
         */
        protected abstract fun findFirstLine(key: String)

        override fun hasNext(): Boolean {
            return this.nextLine != null
        }

        /**
         * Skips over comment lines to find the next line that would be returned
         * by the iterator in a call to [.next].
         *
         * @since JWI 1.0
         */
        protected fun advance() {
            this.nextLine = null

            // check for buffer swap
            if (parentBuffer !== buffer) {
                val pos = itrBuffer.position()
                checkNotNull(buffer)
                val newBuf = buffer!!.asReadOnlyBuffer()
                newBuf.clear()
                newBuf.position(pos)
                itrBuffer = newBuf
            }

            var line: String?
            do {
                line = getLine(itrBuffer, contentType.charset)
            } while (line != null && isComment(line))
            this.nextLine = line
        }

        /**
         * Returns true if the specified line is a comment;
         * false otherwise
         *
         * @param line the line to be tested
         * @return true if the specified line is a comment;
         * false otherwise
         * @since JWI 1.0
         */
        protected fun isComment(line: String): Boolean {
            if (detector == null) {
                return false
            }
            return detector.isCommentLine(line)
        }

        override fun next(): String {
            if (this.nextLine == null) {
                throw NoSuchElementException()
            }
            val result = this.nextLine
            advance()
            return result!!
        }
    }

    companion object {

        /**
         * Returns the String from the current position up to, but not including,
         * the next newline. The buffer's position is set to either directly after
         * the next newline, or the end of the buffer. If the buffer is at its
         * limit, the method returns null. If the buffer's position is directly
         * before a valid newline marker (either \n, \r, or \r\n), then the method
         * returns an empty string.
         *
         * @param buf the buffer from which the line should be extracted
         * @return the remainder of line in the specified buffer, starting from the
         * buffer's current position
         * @throws NullPointerException if the specified buffer is null
         * @since JWI 2.1.0
         */
        @JvmStatic
        fun getLine(buf: ByteBuffer): String? {
            // we are at end of buffer, return null
            val limit = buf.limit()
            if (buf.position() == limit) {
                return null
            }

            val input = StringBuilder()
            var c: Char
            var eol = false

            while (!eol && buf.position() < limit) {
                c = Char(buf.get().toUShort())
                when (c) {
                    '\n' -> eol = true
                    '\r' -> {
                        eol = true
                        val cur = buf.position()
                        c = Char(buf.get().toUShort())
                        if (c != '\n') {
                            buf.position(cur)
                        }
                    }

                    else -> input.append(c)
                }
            }
            return input.toString()
        }

        /**
         * A different version of the getLine method that uses a specified character
         * set to decode the byte stream. If the provided character set is
         * null, the method defaults to the previous method
         * [.getLine].
         *
         * @param buf the buffer from which the line should be extracted
         * @param cs  the character set to use for decoding; may be
         * null
         * @return the remainder of line in the specified buffer, starting from the
         * buffer's current position
         * @throws NullPointerException if the specified buffer is null
         * @since JWI 2.3.4
         */
        @JvmStatic
        fun getLine(buf: ByteBuffer, cs: Charset?): String? {
            // redirect to old method if no charset specified
            var buf = buf
            if (cs == null) {
                return getLine(buf)
            }

            // if we are at end of buffer, return null
            val limit = buf.limit()
            if (buf.position() == limit) {
                return null
            }

            // here we assume that in the character set of the buffer
            // new lines are encoded using the standard ASCII encoding scheme
            // e.g., the single bytes 0x0A or 0x0D, or the two-byte sequence
            // 0x0D0A.  If the byte buffer doesn't follow these conventions,
            // this method will fail.
            var b: Byte
            var eol = false
            val start = buf.position()
            var end = start
            while (!eol && buf.position() < limit) {
                b = buf.get()
                when (b.toInt()) {
                    0x0A -> eol = true
                    0x0D -> {
                        eol = true
                        val cur = buf.position()
                        b = buf.get()
                        if (b.toInt() != 0x0A)  // check for following newline
                        {
                            buf.position(cur)
                        }
                    }

                    else -> end++
                }
            }

            // get sub view containing only the bytes of interest
            // pb with covariant returns if compiled with JSK >=9
            // unless release option is used
            var buf2 = buf.duplicate()
            buf2 = buf2.position(start) as ByteBuffer
            buf2 = buf2.limit(end) as ByteBuffer
            buf = buf2

            // decode the buffer using the provided character set
            return cs.decode(buf).toString()
        }

        /**
         * Rewinds the specified buffer to the beginning of the current line.
         *
         * @param buf the buffer to be rewound; may not be null
         * @throws NullPointerException if the specified buffer is null
         * @since JWI 2.2.0
         */
        @JvmStatic
        fun rewindToLineStart(buf: ByteBuffer) {
            var i = buf.position()

            // check if the buffer is set in the middle of two-char
            // newline marker; if so, back up before it begins
            if (buf.get(i - 1) == '\r'.code.toByte() && buf.get(i) == '\n'.code.toByte()) {
                i--
            }

            // start looking at the character just before
            // the one at which the buffer is set
            if (i > 0) {
                i--
            }

            // walk backwards until we find a newline;
            // if we find a carriage return (CR) or a
            // linefeed (LF), this must be the end of the
            // previous line (either \n, \r, or \r\n)
            var c: Char
            while (i > 0) {
                c = Char(buf.get(i).toUShort())
                if (c == '\n' || c == '\r') {
                    i++
                    break
                }
                i--
            }

            // set the buffer to the beginning of the line
            buf.position(i)
        }
    }
}
