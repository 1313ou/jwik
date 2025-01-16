package edu.mit.jwi.data

import edu.mit.jwi.data.ContentType.Companion.values
import edu.mit.jwi.data.DataType.Companion.find
import edu.mit.jwi.data.IHasLifecycle.ObjectClosedException
import edu.mit.jwi.data.LoadPolicy.BACKGROUND_LOAD
import edu.mit.jwi.data.LoadPolicy.IMMEDIATE_LOAD
import edu.mit.jwi.data.LoadPolicy.NO_LOAD
import edu.mit.jwi.data.compare.ILineComparator
import edu.mit.jwi.item.IHasVersion
import edu.mit.jwi.item.POS
import edu.mit.jwi.item.Synset
import edu.mit.jwi.item.Synset.Companion.zeroFillOffset
import edu.mit.jwi.item.Version
import edu.mit.jwi.item.Version.Companion.NO_VERSION
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Function
import kotlin.Throws

/**
 * Manage access to data source objects.
 * Before the provider can be used, a client must call setSource (or call the appropriate constructor) followed by open.
 * Otherwise, the provider will throw an exception.
 *
 * Implementation of a data provider for Wordnet that uses files in the file system to back instances of its data sources.
 * This implementation takes a URL to a file system directory as its path argument,
 * and uses the resource hints from the data types and parts of speech for its content types to examine the filenames in the that directory to determine which files contain which data.
 *
 * This implementation supports loading the wordnet files into memory, but this is actually not that beneficial for speed.
 * This is because the implementation loads the file data into memory uninterpreted, and on modern machines, the time to interpret a line of data (i.e., parse it into a Java object) is much larger than the time it takes to load the line from disk.
 * Those wishing to achieve speed increases from loading Wordnet into memory should rely on the implementation in RAMDictionary, or something similar, which pre-processes the Wordnet data into objects before caching them.
 */
class FileProvider @JvmOverloads constructor(
    url: URL,
    loadPolicy: Int = NO_LOAD,
    contentTypes: Collection<ContentType<*>> = values(),
) : IHasVersion, IHasLifecycle, IHasCharset, ILoadable {

    private val lifecycleLock: Lock = ReentrantLock()

    private val loadingLock: Lock = ReentrantLock()

    private val prototypeMap: MutableMap<ContentTypeKey, ContentType<*>>

    override var version: Version? = null
        get() {
            checkOpen()
            if (field == null) {
                field = determineVersion(fileMap!!.values)
            }
            if (field === NO_VERSION) {
                return null
            }
            return field
        }

    private var fileMap: Map<ContentType<*>, ILoadableDataSource<*>>? = null

    @Transient
    private var loader: JWIBackgroundLoader? = null

    private val defaultContentTypes: Collection<ContentType<*>>

    private val sourceMatcher: MutableMap<ContentTypeKey, String> = HashMap<ContentTypeKey, String>()

    /**
     * The location of the data.
     * The source URL from which the provider accesses the data from which it instantiates data sources.
     * The data at the specified location may be in an implementation-specific format.
     * If the provider is currently open, this method throws an `IllegalStateException`.
     */
    var source: URL = url
        set(url) {
            check(!isOpen) { "provider currently open" }
            field = url
        }

    /**
     * Returns a data source object for the specified content type, if one is available; otherwise returns null.
     *
     * @param <T> the content type of the data source
     * @param contentType the content type of the data source to be retrieved
     * @return the data source for the specified content type, or null if this provider has no such data source
     * @throws ObjectClosedException if the provider is not open when this call is made
     */
    fun <T> getSource(contentType: ContentType<T>): ILoadableDataSource<T>? {
        checkOpen()

        // assume at first this the prototype
        var actualType = prototypeMap[contentType.key]

        // if this does not map to an adjusted type, we will check under it directly
        if (actualType == null) {
            actualType = contentType
        }
        @Suppress("UNCHECKED_CAST")
        return fileMap!![actualType] as ILoadableDataSource<T>
    }

    val types: Set<ContentType<*>>
        get() {
            try {
                lifecycleLock.lock()
                return LinkedHashSet<ContentType<*>>(prototypeMap.values)
            } finally {
                lifecycleLock.unlock()
            }
        }

    var loadPolicy: Int = 0
        set(policy) {
            try {
                loadingLock.lock()
                field = policy
            } finally {
                loadingLock.unlock()
            }
        }

    /**
     * Sets the character set associated with this dictionary.
     *
     * @param charset the possibly null character set to use when decoding files.
     * @throws IllegalStateException if the provider is currently open
     */
    override var charset: Charset? = null
        set(charset) {
            if (verbose) {
                System.out.printf("Charset: %s%n", charset)
            }
            try {
                lifecycleLock.lock()
                check(!isOpen) { "provider currently open" }
                for (e in prototypeMap.entries) {
                    val key: ContentTypeKey = e.key
                    val value = e.value
                    if (charset == null) {
                        // if we get a null charset, reset to the prototype value but preserve line comparator
                        val defaultContentType: ContentType<*> = getDefault(key)!!
                        e.setValue(ContentType<Any?>(key, value.lineComparator, defaultContentType.charset))
                    } else {
                        // if we get a non-null charset, generate new  type using the new charset but preserve line comparator
                        e.setValue(ContentType<Any?>(key, value.lineComparator, charset))
                    }
                }
                field = charset
            } finally {
                lifecycleLock.unlock()
            }
        }

    /**
     * Constructs the file provider pointing to the resource indicated by the path.
     * This file provider has an initial NO_LOAD load policy.
     *
     * @param file A file pointing to the wordnet directory
     */
    constructor(file: File) : this(toURL(file))

    /**
     * Constructs the file provider pointing to the resource indicated by the path, with the specified load policy.
     *
     * @param file A file pointing to the wordnet directory
     * @param loadPolicy the load policy for this provider; this provider supports the three values defined in ILoadPolicy.
     */
    constructor(file: File, loadPolicy: Int) : this(toURL(file), loadPolicy, values())

    /**
     * Constructs the file provider pointing to the resource indicated by the path, with the specified load policy, looking for the specified content type.
     *
     * @param file A file pointing to the wordnet directory
     * @param loadPolicy the load policy for this provider; this provider supports the three values defined in ILoadPolicy.
     * @param types the content types this provider will look for when it loads its data; may not be empty
     * @throws IllegalArgumentException if the set of types is empty
     */
    constructor(file: File, loadPolicy: Int, types: MutableCollection<out ContentType<*>>) : this(toURL(file), loadPolicy, types)

    init {
        require(!contentTypes.isEmpty())
        this.defaultContentTypes = contentTypes
        this.prototypeMap = contentTypes
            .asSequence()
            .map { it.key to it }
            .toMap()
            .toMutableMap()
    }

    private fun getDefault(key: ContentTypeKey?): ContentType<*>? {
        for (contentType in this.defaultContentTypes) {
            if (contentType.key == key) {
                return contentType
            }
        }
        // this should not happen
        return null
    }

    /**
     * Sets the comparator associated with this content type in this dictionary.
     * The comparator may be null in which case it is reset.
     *
     * @param contentTypeKey the content type key for which the comparator is to be set.
     * @param comparator the possibly null comparator to use when decoding files.
     * @throws IllegalStateException if the provider is currently open
     */
    fun setComparator(contentTypeKey: ContentTypeKey, comparator: ILineComparator?) {
        if (verbose) {
            System.out.printf("Comparator for %s %s%n", contentTypeKey, comparator?.javaClass?.name)
        }
        try {
            lifecycleLock.lock()
            check(!isOpen) { "Provider currently open" }
            val value: ContentType<*> = prototypeMap[contentTypeKey]!!
            if (comparator == null) {
                // if we get a null comparator, reset to the prototype but preserve charset
                val defaultContentType: ContentType<*>? = getDefault(contentTypeKey)
                prototypeMap.put(contentTypeKey, ContentType<Any?>(contentTypeKey, defaultContentType!!.lineComparator, value.charset))
            } else {
                // if we get a non-null comparator, generate a new type using the new comparator but preserve charset
                prototypeMap.put(contentTypeKey, ContentType<Any?>(contentTypeKey, comparator, value.charset))
            }
        } finally {
            lifecycleLock.unlock()
        }
    }

    /**
     * Sets pattern attached to content type key, that source files have to match to be selected.
     * This gives selection a first opportunity before falling back on standard data type selection.
     *
     * @param contentTypeKey the content type key for which the matcher is to be set.
     * @param pattern regexp pattern
     */
    fun setSourceMatcher(contentTypeKey: ContentTypeKey, pattern: String?) {
        if (verbose) {
            System.out.printf("Matcher for %s: '%s'%n", contentTypeKey, pattern)
        }
        try {
            lifecycleLock.lock()
            check(!isOpen) { "provider currently open" }
            if (pattern == null) {
                sourceMatcher.remove(contentTypeKey)
            } else {
                sourceMatcher.put(contentTypeKey, pattern)
            }
        } finally {
            lifecycleLock.unlock()
        }
    }

    /**
     * Returns the first content type, if any, that matches the specified data type and pos object.
     *
     * @param <T> type
     * @param dt the data type, possibly null, of the desired content type
     * @param pos the part of speech, possibly null, of the desired content type
     * @return the first content type that matches the specified data type and part of speech.
     */
    fun <T> resolveContentType(dt: DataType<T>?, pos: POS?): ContentType<T>? {
        for (e in prototypeMap.entries) {
            if (e.key.getDataType<Any?>() == dt && e.key.pOS == pos) {
                @Suppress("UNCHECKED_CAST")
                return e.value as ContentType<*>? as ContentType<T>?
            }
        }
        return null
    }

    @Throws(IOException::class)
    override fun open(): Boolean {
        try {
            lifecycleLock.lock()
            loadingLock.lock()

            val policy = loadPolicy

            // make sure directory exists
            val directory: File = toFile(source)
            if (!directory.exists()) {
                throw IOException("Dictionary directory does not exist: $directory")
            }

            // get files in directory
            val fileArray = directory.listFiles(FileFilter { obj: File? -> obj!!.isFile })
            if (fileArray == null || fileArray.size == 0) {
                throw IOException("No files found in $directory")
            }
            val files: MutableList<File> = ArrayList<File>(listOf<File?>(*fileArray))
            if (files.isEmpty()) {
                throw IOException("No files found in $directory")
            }

            // sort them
            files.sortWith(Comparator.comparing<File?, String?>(Function { obj: File? -> obj!!.name }))

            // make the source map
            var hiddenMap = createSourceMap(files, policy)
            if (hiddenMap.isEmpty()) {
                return false
            }

            // determine if it's already unmodifiable, wrap if not
            val map: MutableMap<*, *> = mutableMapOf<Any?, Any?>()
            if (hiddenMap.javaClass != map.javaClass) {
                hiddenMap = Collections.unmodifiableMap<ContentType<*>, ILoadableDataSource<*>>(hiddenMap)
            }
            @Suppress("UNCHECKED_CAST")
            fileMap = hiddenMap as Map<ContentType<*>, ILoadableDataSource<*>>?

            // do load
            try {
                when (loadPolicy) {
                    BACKGROUND_LOAD -> load(false)
                    IMMEDIATE_LOAD  -> load(true)
                    else            -> {}
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            return true
        } finally {
            lifecycleLock.unlock()
            loadingLock.unlock()
        }
    }

    override fun load() {
        try {
            load(false)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    @Throws(InterruptedException::class)
    override fun load(block: Boolean) {
        try {
            loadingLock.lock()
            checkOpen()
            if (isLoaded) {
                return
            }
            if (loader != null) {
                return
            }
            loader = JWIBackgroundLoader()
            loader!!.start()
            if (block) {
                loader!!.join()
            }
        } finally {
            loadingLock.lock()
        }
    }

    override val isLoaded: Boolean
        get() {
            check(isOpen) { "provider not open" }
            try {
                loadingLock.lock()
                for (source in fileMap!!.values) {
                    if (!source.isLoaded) {
                        return false
                    }
                }
                return true
            } finally {
                loadingLock.unlock()
            }
        }

    /**
     * Creates the map that contains the content types mapped to the data sources.
     * The method should return a non-null result, but it may be empty if no data sources can be created.
     * Subclasses may override this method.
     *
     * @param files the files from which the data sources should be created
     * @param policy the load policy of the provider
     * @return a map, possibly empty, of content types mapped to data sources
     * @throws IOException if there is a problem creating the data source
     */
    @Throws(IOException::class)
    private fun createSourceMap(files: MutableList<File>, policy: Int): MutableMap<ContentType<*>?, ILoadableDataSource<*>> {
        val result: MutableMap<ContentType<*>?, ILoadableDataSource<*>> = HashMap<ContentType<*>?, ILoadableDataSource<*>>()
        for (contentType in prototypeMap.values) {
            var file: File? = null

            // give first chance to matcher
            if (sourceMatcher.containsKey(contentType.key)) {
                val regex = sourceMatcher[contentType.key]!!
                file = match(regex, files)
            }

            // if it failed fall back on data types
            if (file == null) {
                val dataType: DataType<*> = contentType.dataType
                file = find(dataType, contentType.pOS, files)
            }

            // if it failed continue
            if (file == null) {
                continue
            }

            // do not remove file from possible choices as both content types may use the same file
            if ((contentType.key != ContentTypeKey.SENSE) &&
                (contentType.key != ContentTypeKey.SENSES) &&
                (contentType.key != ContentTypeKey.INDEX_ADJECTIVE) &&
                (contentType.key != ContentTypeKey.INDEX_ADVERB) &&
                (contentType.key != ContentTypeKey.INDEX_NOUN) &&
                (contentType.key != ContentTypeKey.INDEX_VERB)
            ) {
                files.remove(file)
            }

            result.put(contentType, createDataSource(file, contentType, policy))
            if (verbose) {
                System.out.printf("%s %s%n", contentType, file.name)
            }
        }
        return result
    }

    private fun match(pattern: String, files: MutableList<File>): File? {
        for (file in files) {
            val name = file.name
            if (name.matches(pattern.toRegex())) {
                return file
            }
        }
        return null
    }

    /**
     * Creates the actual data source implementations.
     *
     * @param <T> the content type of the data source
     * @param file the file from which the data source should be created
     * @param contentType the content type of the data source
     * @param policy the load policy to follow when creating the data source
     * @return the created data source
     * @throws IOException if there is an IO problem when creating the data source
     */
    @Throws(IOException::class)
    private fun <T> createDataSource(file: File, contentType: ContentType<T>, policy: Int): ILoadableDataSource<T> {
        var src: ILoadableDataSource<T>
        if (contentType.dataType === DataType.DATA) {
            src = createDirectAccess<T>(file, contentType)
            src.open()
            if (policy == IMMEDIATE_LOAD) {
                try {
                    src.load(true)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }

            // check to see if direct access works with the file
            // often people will extract the files incorrectly on Windows machines and the binary files will be corrupted with extra CRs

            // get first line
            val itr: Iterator<String?> = src.iterator()
            val firstLine = itr.next()
            if (firstLine == null) {
                return src
            }

            // extract key
            val parser = contentType.dataType.parser
            val s = parser.parseLine(firstLine) as Synset
            val key = zeroFillOffset(s.offset)

            // try to find line by direct access
            val soughtLine = src.getLine(key)
            if (soughtLine != null) {
                return src
            }

            val pos: POS? = contentType.pOS
            System.err.println("${System.currentTimeMillis()} - Error on direct access in $pos data file: check CR/LF endings")
        }

        src = createBinarySearch<T>(file, contentType)
        src.open()
        if (policy == IMMEDIATE_LOAD) {
            try {
                src.load(true)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        return src
    }

    /**
     * Creates a direct access data source for the specified type, using the specified file.
     *
     * @param <T> the parameter of the content type
     * @param file the file on which the data source is based
     * @param contentType the data type for the data source
     * @return the data source
     */
    private fun <T> createDirectAccess(file: File, contentType: ContentType<T>): ILoadableDataSource<T> {
        return DirectAccessWordnetFile<T>(file, contentType)
    }

    /**
     * Creates a binary search data source for the specified type, using the specified file.
     *
     * @param <T> the parameter of the content type
     * @param file the file on which the data source is based
     * @param contentType the data type for the data source
     * @return the data source
     */
    private fun <T> createBinarySearch(file: File, contentType: ContentType<T>): ILoadableDataSource<T> {
        return if ("Word" == contentType.dataType.toString()) BinaryStartSearchWordnetFile<T>(file, contentType) else BinarySearchWordnetFile<T>(file, contentType)
    }

    override val isOpen: Boolean
        get() {
            try {
                lifecycleLock.lock()
                return fileMap != null
            } finally {
                lifecycleLock.unlock()
            }
        }

    override fun close() {
        try {
            lifecycleLock.lock()
            if (!isOpen) {
                return
            }
            if (loader != null) {
                loader!!.cancel()
            }
            for (source in fileMap!!.values) {
                source.close()
            }
            fileMap = null
        } finally {
            lifecycleLock.unlock()
        }
    }

    /**
     * Convenience method that throws an exception if the provider is closed.
     *
     * @throws ObjectClosedException if the provider is closed
     */
    private fun checkOpen() {
        if (!isOpen) {
            throw ObjectClosedException()
        }
    }

    /**
     * A thread class which tries to load each data source in this provider.
     */
    private inner class JWIBackgroundLoader : Thread() {

        @Transient
        private var cancel = false

        /**
         * Constructs a new background loader that operates on the internal data structures of this provider.
         */
        init {
            name = JWIBackgroundLoader::class.java.simpleName
            isDaemon = true
        }

        override fun run() {
            try {
                for (source in fileMap!!.values) {
                    if (!cancel && !source.isLoaded) {
                        try {
                            source.load(true)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    }
                }
            } finally {
                loader = null
            }
        }

        /**
         * Sets the cancel flag for this loader.
         */
        fun cancel() {
            cancel = true
            try {
                join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Determines a version from the set of data sources, if possible, otherwise returns NO_VERSION
     *
     * @param srcs the data sources to be used to determine the version
     * @return the single version that describes these data sources, or NO_VERSION if there is none
     */
    private fun determineVersion(srcs: Collection<IDataSource<*>>): Version? {
        var ver: Version? = NO_VERSION
        for (dataSrc in srcs) {
            // if no version to set, ignore
            if (dataSrc.version == null) {
                continue
            }

            // init version
            if (ver === NO_VERSION) {
                ver = dataSrc.version
                continue
            }

            // if version different from current
            if (ver != dataSrc.version) {
                return NO_VERSION
            }
        }
        return ver
    }

    companion object {

        var verbose: Boolean = false

        /**
         * Transforms a URL into a File. The URL must use the 'file' protocol and must be in a UTF-8 compatible format as specified in URLDecoder.
         *
         * @param url url
         * @return a file pointing to the same place as the url
         * @throws IllegalArgumentException if the url does not use the 'file' protocol
         */
        fun toFile(url: URL): File {
            require(url.protocol == "file") { "URL source must use 'file' protocol" }
            try {
                return File(URLDecoder.decode(url.path, "UTF-8"))
            } catch (e: UnsupportedEncodingException) {
                throw RuntimeException(e)
            }
        }

        /**
         * Transforms a file into a URL.
         *
         * @param file the file to be transformed
         * @return a URL representing the file
         */
        fun toURL(file: File): URL {
            val uri = URI("file", "//", file.toURI().toURL().path, null)
            return URL("file", null, uri.rawPath)
        }

        /**
         * A utility method for checking whether a file represents an existing local directory.
         *
         * @param url the url object to check
         * @return whether url object represents a local directory which exists
         */
        fun isLocalDirectory(url: URL): Boolean {
            if (url.protocol != "file") {
                return false
            }
            val file: File = toFile(url)
            return isLocalDirectory(file)
        }

        /**
         * A utility method for checking whether a file represents an existing local directory.
         *
         * @param dir the file object to check
         * @return whether the file object represents a local directory which exist
         */
        fun isLocalDirectory(dir: File): Boolean {
            return dir.exists() && dir.isDirectory
        }

        /**
         * A utility method for checking whether a file represents an existing local file.
         *
         * @param url the url object to check
         * @return whether the url object represents a local file which exists
         */
        fun isLocalFile(url: URL): Boolean {
            if (url.protocol != "file") {
                return false
            }
            val file: File = toFile(url)
            return isLocalFile(file)
        }

        /**
         * A utility method for checking whether a file represents an existing local file
         *
         * @param file the file object to check
         * @return whether the file object represents a local file which exist
         */
        fun isLocalFile(file: File): Boolean {
            return file.exists() && file.isFile
        }
    }
}
