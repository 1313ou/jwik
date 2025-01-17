package edu.mit.jwi.data

import edu.mit.jwi.data.IHasLifecycle.ObjectClosedException
import edu.mit.jwi.data.compare.ICommentDetector
import edu.mit.jwi.item.Version
import edu.mit.jwi.item.Version.Companion.NO_VERSION
import edu.mit.jwi.item.Version.Companion.extractVersion
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * Abstract superclass of wordnet data file objects.
 * Provides all the infrastructure required to access the files, except for the construction of iterators and the actual implementation of the getLine method.
 *
 * While this object is implemented to provide load/unload capabilities (i.e., it allows the whole wordnet file to be loaded into memory, rather than read from disk), this does not provide much of a performance boost.
 * In tests, the time for parsing a line of data into a data object dominates the time required to read the data from disk (for a reasonable modern hard drive).
 *
 * Constructs an instance of this class backed by the specified java File object, with the specified content type.
 * No effort is made to ensure that the data in the specified file is actually formatted in the proper manner for the line parser associated with the content type's data type.
 * If these are mismatched, this will result in MisformattedLineExceptions in later calls.
 *
 * @param file the file which backs this wordnet file
 * @param contentType the content type for this file
 * @param <T> the type of the objects represented in this file
 */
abstract class WordnetFile<T>(
    /**
     * The file which backs this object.
     */
    val file: File,

    /**
     * The content type
     */
    override val contentType: ContentType<T>,

    ) : ILoadableDataSource<T> {

    override val name: String
        get() = file.name

    private val commentDetector: ICommentDetector?
        get() = contentType.lineComparator!!.commentDetector

    // loading locks and status flag the flag is marked transient to avoid different values in different threads
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
     */
    fun getBuffer(): ByteBuffer {
        if (!isOpen) {
            throw ObjectClosedException()
        }
        return buffer!!
    }

    /**
     * Returns the wordnet version associated with this object, or null if the version cannot be determined.
     *
     * @return the wordnet version associated with this object, or null if the version cannot be determined
     * @throws ObjectClosedException if the object is closed when this method is called
     */
    override var version: Version? = null
        get() {
            if (!isOpen) {
                throw ObjectClosedException()
            }
            if (field == null) {
                val v = extractVersion(contentType, buffer!!.asReadOnlyBuffer())
                if (v == null) {
                    field = NO_VERSION
                }
            }
            return if (field === NO_VERSION) null else field
        }

    // H A S H   +   E Q U A L S

    override fun hashCode(): Int {
        return Objects.hash(contentType, file)
    }

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
        if (contentType != other.contentType) {
            return false
        }
        return file == other.file
    }

    // O P E N

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

    // L O A D

    override fun load() {
        load(false)
    }

    override fun load(block: Boolean) {
        try {
            loadingLock.lock()
            val len = file.length().toInt()

            val roBuffer = buffer!!.asReadOnlyBuffer()
            roBuffer.clear()

            val data = ByteArray(len)
            roBuffer.get(data, 0, len)

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

    // I T E R A T E

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
     * Constructs an iterator that can be used to iterate over the specified ByteBuffer, starting from the specified key.
     *
     * @param buffer the buffer over which the iterator will iterate
     * @param key the key at which the iterator should begin
     * @return an iterator that can be used to iterate over the lines of the ByteBuffer
     */
    abstract fun makeIterator(buffer: ByteBuffer, key: String?): LineIterator

    /**
     * Used to iterate over lines in a file.
     * It is a look-ahead iterator.
     *
     * Constructs a new line iterator over this buffer, starting at the specified key.
     *
     * @param buffer the buffer over which the iterator should iterate
     */
    abstract inner class LineIterator(buffer: ByteBuffer) : Iterator<String> {

        protected val parentBuffer: ByteBuffer = buffer

        @JvmField
        protected var itrBuffer: ByteBuffer

        init {
            itrBuffer = buffer.asReadOnlyBuffer()
            itrBuffer.clear()
        }

        /**
         * The line currently stored as the 'next' line, if any.
         * The next line that will be parsed and returned by this iterator, or null if none
         * Is a pure getter; does not increment the iterator.
         */
        var nextLine: String? = null
            protected set

        override fun hasNext(): Boolean {
            return nextLine != null
        }

        override fun next(): String {
            if (nextLine == null) {
                throw NoSuchElementException()
            }
            val result = nextLine
            advance()
            return result!!
        }

        /**
         * Start at the specified key.
         *
         * @param key0 the key of the line to start at
         */
        fun startAt(key0: String?) {
            var key = key0?.trim { it <= ' ' }
            if (key == null || key.isEmpty()) {
                advance()
            } else {
                findFirstLine(key)
            }
        }

        /**
         * Advances the iterator the first line the iterator should return, based on the specified key.
         * If the key is not found in the file, it will advance the iterator past all lines.
         *
         * @param key the key indexed the first line to be returned by the iterator
         */
        protected abstract fun findFirstLine(key: String)

        /**
         * Skips over comment lines to find the next line that would be returned by the iterator in a call to next.
         */
        protected fun advance() {
            nextLine = null

            // check for buffer swap
            if (parentBuffer !== buffer) {
                val pos = itrBuffer.position()
                val newBuffer = buffer!!.asReadOnlyBuffer()
                newBuffer.clear()
                newBuffer.position(pos)
                itrBuffer = newBuffer
            }

            var line: String?
            do {
                line = getLine(itrBuffer, contentType.charset)
            } while (line != null && isComment(line))
            nextLine = line
        }

        /**
         * Whether the specified line is a comment
         *
         * @param line the line to be tested
         * @return whether if the specified line is a comment
         */
        protected fun isComment(line: String): Boolean {
            return commentDetector?.isCommentLine(line) == true
        }
    }

    companion object {

        /**
         * The String from the current position up to, but not including, the next newline.
         * The buffer's position is set to either directly after the next newline, or the end of the buffer.
         * If the buffer is at its limit, the method returns null.
         * If the buffer's position is directly before a valid newline marker (either \n, \r, or \r\n), then the method returns an empty string.
         *
         * @param buf the buffer from which the line should be extracted
         * @return the remainder of line in the specified buffer, starting from the buffer's current position
         */
        @JvmStatic
        fun getLine(buf: ByteBuffer): String? {
            // we are at end of buffer, return null
            val limit = buf.limit()
            if (buf.position() == limit) {
                return null
            }

            val input = StringBuilder()

            var eol = false
            while (!eol && buf.position() < limit) {
                var c = Char(buf.get().toUShort())
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
         * A different version of the getLine method that uses a specified character set to decode the byte stream.
         * If the provided character set is null, the method defaults to the previous method getLine.
         *
         * @param buf the buffer from which the line should be extracted
         * @param cs the character set to use for decoding
         * @return the remainder of line in the specified buffer, starting from the buffer's current position
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

            // here we assume that in the character set of the buffer new lines are encoded using the standard ASCII encoding scheme
            // e.g., the single bytes 0x0A or 0x0D, or the two-byte sequence 0x0D0A.
            // If the byte buffer doesn't follow these conventions, this method will fail.
            var eol = false
            val start = buf.position()
            var end = start
            while (!eol && buf.position() < limit) {
                var b = buf.get()
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

            // get sub view containing only the bytes of interest pb with covariant returns if compiled with JDK >=9 unless release option is used
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
         * @param buf the buffer to be rewound
         */
        @JvmStatic
        fun rewindToLineStart(buf: ByteBuffer) {
            var i = buf.position()

            // check if the buffer is set in the middle of two-char newline marker; if so, back up before it begins
            if (buf.get(i - 1) == '\r'.code.toByte() && buf.get(i) == '\n'.code.toByte()) {
                i--
            }

            // start looking at the character just before the one at which the buffer is set
            if (i > 0) {
                i--
            }

            // walk backwards until we find a newline;
            // if we find a carriage return (CR) or a linefeed (LF), this must be the end of the previous line (either \n, \r, or \r\n)
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
