package edu.mit.jwi

import edu.mit.jwi.data.FileProvider.Companion.isLocalFile
import edu.mit.jwi.data.IHasLifecycle
import edu.mit.jwi.data.LoadPolicy
import edu.mit.jwi.item.Version
import java.io.*
import java.net.URL
import java.util.zip.GZIPInputStream

/**
 * Dictionary that deserializes dictionary object.
 * @property streamFactory dictionary that backs this dictionary
 * @property loadPolicy immutable IMMEDIATE_LOAD load policy
 * @param config configuration bundle
 */
class DeserializedRAMDictionary
@JvmOverloads
constructor(
    /**
     * The stream factory that backs this instance
     */
    val streamFactory: IInputStreamFactory,
    /**
     * Config bundle
     */
    config: Config? = null,

    ) : BaseRAMDictionary() {

    var loadPolicy: Int = LoadPolicy.IMMEDIATE_LOAD
        set(_) {
            if (isOpen)
                throw IHasLifecycle.ObjectOpenException()
            // if the dictionary uses an input stream factory the load policy is effectively IMMEDIATE_LOAD so the load policy is set to this for information purposes
            loadPolicy = LoadPolicy.IMMEDIATE_LOAD
        }

    override val version: Version?
        get() {
            if (data != null) {
                return data!!.version
            }
            return null
        }

    /**
     * Loads data from the specified File using the specified load policy.
     *
     * Constructs a new wrapper RAM dictionary that will load the contents the specified local Wordnet data, with the specified load policy.
     *
     * @param file a file pointing to a local copy of Wordnet
     * @param config config bundle
     */
    @JvmOverloads
    constructor(
        file: String,
        config: Config? = null,
    ) : this(createInputStreamFactory(File(file)), config)

    /**
     * Loads data from the specified File using the specified load policy.
     *
     * Constructs a new wrapper RAM dictionary that will load the contents the specified local Wordnet data, with the specified load policy.
     *
     * @param file a file pointing to a local copy of Wordnet
     * @param config config bundle
     */
    @JvmOverloads
    constructor(
        file: File,
        config: Config? = null,
    ) : this(createInputStreamFactory(file), config)

    /**
     * Loads data from the specified URL using the specified load policy.
     *
     * Constructs a new RAMDictionary that will load the contents the specified Wordnet data using the default load policy.
     *
     * @param url an url pointing to a local copy of Wordnet
     * @param config config bundle
     */
    @JvmOverloads
    constructor(
        url: URL,
        config: Config? = null,
    ) : this(createInputStreamFactory(url), config)

    init {
        configure(config)
    }

    override fun configure(config: Config?) {
    }

    override fun startLoad(): Boolean {

        // behavior when loading from an input stream is immediate load
        try {
            load(true)
        } catch (e: InterruptedException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * This thread loads the dictionary data into memory and sets the appropriate variable in the parent dictionary.
     */
    override fun makeThread(): Thread {
        val t = Thread {
            try {
                streamFactory.makeInputStream().use {
                    GZIPInputStream(it).use {
                        BufferedInputStream(it).use {
                            ObjectInputStream(it).use {
                                data = it.readObject() as DictionaryData
                            }
                        }
                    }
                }
            } catch (t: Throwable) {
                if (!Thread.currentThread().isInterrupted) {
                    t.printStackTrace()
                    System.err.println("Unable to load dictionary data into memory")
                }
            }
        }
        t.name = "Serialized Data Loader"
        return t
    }

    interface IInputStreamFactory {

        /**
         * Returns a new input stream from this factory.
         *
         * @return a new, unused input stream from this factory.
         * @throws IOException io exception
         */
        @Throws(IOException::class)
        fun makeInputStream(): InputStream
    }

    /**
     * Default implementation of the IInputStreamFactory interface which creates an input stream from a specified File object.
     *
     * Creates a FileInputStreamFactory that uses the specified file.
     *
     * @param file the file from which the input streams should be created
     */
    class FileInputStreamFactory(private val file: File) : IInputStreamFactory {

        @Throws(IOException::class)
        override fun makeInputStream(): InputStream {
            return FileInputStream(file)
        }
    }

    /**
     * Default implementation of the IInputStreamFactory interface which creates an input stream from a specified URL.
     *
     * Creates a URLInputStreamFactory that uses the specified url.
     *
     * @param url the url from which the input streams should be created
     */
    class URLInputStreamFactory(val url: URL) : IInputStreamFactory {

        @Throws(IOException::class)
        override fun makeInputStream(): InputStream {
            return url.openStream()
        }
    }

    companion object {

        const val NOT_LOCAL = "Not a local file"

        /**
         * Creates an input stream factory out of the specified File. If the file
         * points to a local directory then the method returns null.
         *
         * @param file the file out of which to make an input stream factory
         * @return a new input stream factory, or null if the url points to a local directory.
         */
        fun createInputStreamFactory(file: File): IInputStreamFactory {
            if (!isLocalFile(file)) throw RuntimeException(NOT_LOCAL)
            return FileInputStreamFactory(file)
        }

        /**
         * Creates an input stream factory out of the specified URL. If the url
         * points to a local directory then the method returns null.
         *
         * @param url the url out of which to make an input stream factory
         * @return a new input stream factory, or null if the url points to a local directory.
         */
        fun createInputStreamFactory(url: URL): IInputStreamFactory {
            if (!isLocalFile(url)) throw RuntimeException(NOT_LOCAL)
            return URLInputStreamFactory(url)
        }
    }
}