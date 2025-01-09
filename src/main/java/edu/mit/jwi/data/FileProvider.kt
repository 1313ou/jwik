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

import edu.mit.jwi.NonNull
import edu.mit.jwi.Nullable
import edu.mit.jwi.data.ContentType.Companion.values
import edu.mit.jwi.data.DataType.Companion.find
import edu.mit.jwi.data.IHasLifecycle.ObjectClosedException
import edu.mit.jwi.data.compare.ILineComparator
import edu.mit.jwi.item.ISynset
import edu.mit.jwi.item.IVersion
import edu.mit.jwi.item.POS
import edu.mit.jwi.item.Synset.Companion.zeroFillOffset
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
 *
 *
 * Implementation of a data provider for Wordnet that uses files in the file
 * system to back instances of its data sources. This implementation takes a
 * `URL` to a file system directory as its path argument, and uses
 * the resource hints from the data types and parts of speech for its content
 * types to examine the filenames in the that directory to determine which files
 * contain which data.
 *
 *
 *
 * This implementation supports loading the wordnet files into memory,
 * but this is actually not that beneficial for speed. This is because the
 * implementation loads the file data into memory uninterpreted, and on modern
 * machines, the time to interpret a line of data (i.e., parse it into a Java
 * object) is much larger than the time it takes to load the line from disk.
 * Those wishing to achieve speed increases from loading Wordnet into memory
 * should rely on the implementation in [RAMDictionary], or something
 * similar, which pre-processes the Wordnet data into objects before caching
 * them.
 *
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
class FileProvider @JvmOverloads constructor(
    url: URL,
    loadPolicy: Int = ILoadPolicy.Companion.NO_LOAD,
    contentTypes: Collection<IContentType<*>> = values(),
) : IDataProvider, ILoadable, ILoadPolicy {

    // final instance fields
    private val lifecycleLock: Lock = ReentrantLock()

    private val loadingLock: Lock = ReentrantLock()

    private val prototypeMap: MutableMap<ContentTypeKey, IContentType<*>>

    override var version: IVersion? = null
        get() {
            checkOpen()
            if (field == null) {
                checkNotNull(fileMap)
                field = determineVersion(fileMap!!.values)
            }
            if (field === IVersion.NO_VERSION) {
                return null
            }
            return field
        }
        private set

    private var fileMap: Map<IContentType<*>, ILoadableDataSource<*>>? = null

    @Nullable
    @Transient
    private var loader: JWIBackgroundLoader? = null

    @NonNull
    private val defaultContentTypes: Collection<IContentType<*>>

    @NonNull
    private val sourceMatcher: MutableMap<ContentTypeKey, String> = HashMap<ContentTypeKey, String>()

    override var source: URL = url
        set(url) {
            check(!isOpen) { "provider currently open" }
            field = url
        }

    override var loadPolicy: Int = 0
        set(policy) {
            try {
                loadingLock.lock()
                field = policy
            } finally {
                loadingLock.unlock()
            }
        }

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
                        val defaultContentType: IContentType<*> = checkNotNull(getDefault(key))
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

    //override fun setCharset(charset: Charset?){
    //    this.charset = charset
    //}

    /**
     * Constructs the file provider pointing to the resource indicated by the
     * path.  This file provider has an initial [ILoadPolicy.NO_LOAD] load policy.
     *
     * @param file A file pointing to the wordnet directory, may not be
     * `null`
     * @throws NullPointerException if the specified file is `null`
     * @since JWI 1.0
     */
    constructor(file: File) : this(toURL(file))

    /**
     * Constructs the file provider pointing to the resource indicated by the
     * path, with the specified load policy.
     *
     * @param file       A file pointing to the wordnet directory, may not be
     * `null`
     * @param loadPolicy the load policy for this provider; this provider supports the
     * three values defined in `ILoadPolicy`.
     * @throws NullPointerException if the specified file is `null`
     * @since JWI 2.2.0
     */
    constructor(file: File, loadPolicy: Int) : this(toURL(file)!!, loadPolicy, values())

    /**
     * Constructs the file provider pointing to the resource indicated by the
     * path, with the specified load policy, looking for the specified content
     * type.s
     *
     * @param file       A file pointing to the wordnet directory, may not be
     * `null`
     * @param loadPolicy the load policy for this provider; this provider supports the
     * three values defined in `ILoadPolicy`.
     * @param types      the content types this provider will look for when it loads
     * its data; may not be `null` or empty
     * @throws NullPointerException     if the file or content type collection is `null`
     * @throws IllegalArgumentException if the set of types is empty
     * @since JWI 2.2.0
     */
    constructor(file: File, loadPolicy: Int, @NonNull types: MutableCollection<out IContentType<*>>) : this(toURL(file)!!, loadPolicy, types)

    /**
     * Constructs the file provider pointing to the resource indicated by the
     * path, with the specified load policy, looking for the specified content
     * type.s
     *
     * @param url          A file URL in UTF-8 decodable format, may not be
     * `null`
     * @param loadPolicy   the load policy for this provider; this provider supports the
     * three values defined in `ILoadPolicy`.
     * @param contentTypes the content types this provider will look for when it loads
     * its data; may not be `null` or empty
     * @throws NullPointerException     if the url or content type collection is `null`
     * @throws IllegalArgumentException if the set of types is empty
     * @since JWI 2.2.0
     */
    /**
     * Constructs the file provider pointing to the resource indicated by the
     * path, with the specified load policy.
     *
     * @param url        A file URL in UTF-8 decodable format, may not be
     * `null`
     * @param loadPolicy the load policy for this provider; this provider supports the
     * three values defined in `ILoadPolicy`.
     * @throws NullPointerException if the specified URL is `null`
     * @since JWI 2.2.0
     */
    /**
     * Constructs the file provider pointing to the resource indicated by the
     * path.  This file provider has an initial [ILoadPolicy.NO_LOAD] load policy.
     *
     * @param url A file URL in UTF-8 decodable format, may not be
     * `null`
     * @throws NullPointerException if the specified URL is `null`
     * @since JWI 1.0
     */
    init {
        if (url == null) {
            throw NullPointerException()
        }
        require(!contentTypes.isEmpty())
        this.defaultContentTypes = contentTypes

        val prototypeMap: MutableMap<ContentTypeKey, IContentType<*>> = LinkedHashMap<ContentTypeKey, IContentType<*>>(contentTypes.size)
        for (contentType in contentTypes) {
            val key = contentType.key
            prototypeMap.put(key, contentType)
        }
        this.prototypeMap = prototypeMap
    }

    @Nullable
    private fun getDefault(key: ContentTypeKey?): IContentType<*>? {
        for (contentType in this.defaultContentTypes) {
            if (contentType.key == key) {
                return contentType
            }
        }
        // this should not happen
        return null
    }

    override fun setComparator(key: ContentTypeKey, comparator: ILineComparator?) {
        if (verbose) {
            checkNotNull(comparator)
            System.out.printf("Comparator for %s %s%n", key, comparator.javaClass.getName())
        }
        try {
            lifecycleLock.lock()
            check(!isOpen) { "provider currently open" }
            val value: IContentType<*> = prototypeMap.get(key)!!
            if (comparator == null) {
                // if we get a null comparator, reset to the prototype but preserve charset
                val defaultContentType: IContentType<*>? = checkNotNull(getDefault(key))
                checkNotNull(value)
                prototypeMap.put(key, ContentType<Any?>(key, defaultContentType!!.lineComparator, value.charset))
            } else {
                // if we get a non-null comparator, generate a new type using the new comparator but preserve charset
                checkNotNull(value)
                prototypeMap.put(key, ContentType<Any?>(key, comparator, value.charset))
            }
        } finally {
            lifecycleLock.unlock()
        }
    }

    override fun setSourceMatcher(key: ContentTypeKey, pattern: String?) {
        if (verbose) {
            System.out.printf("Matcher for %s: '%s'%n", key, pattern)
        }
        try {
            lifecycleLock.lock()
            check(!isOpen) { "provider currently open" }
            if (pattern == null) {
                sourceMatcher.remove(key)
            } else {
                sourceMatcher.put(key, pattern)
            }
        } finally {
            lifecycleLock.unlock()
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.edu.mit.jwi.data.IDataProvider#resolveContentType(edu.edu.mit.jwi.data.IDataType, edu.edu.mit.jwi.item.POS)
     */
    override fun <T> resolveContentType(dt: IDataType<T>, pos: POS?): IContentType<T>? {
        for (e in prototypeMap.entries) {
            if (e.key.getDataType<Any?>() == dt && e.key.pOS == pos) {
                return e.value as IContentType<*>? as IContentType<T>?
            }
        }
        return null
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
            loadingLock.lock()

            val policy = loadPolicy

            // make sure directory exists
            checkNotNull(source)
            val directory: File = toFile(source)
            if (!directory.exists()) {
                throw IOException("Dictionary directory does not exist: $directory")
            }

            // get files in directory
            val fileArray = directory.listFiles(FileFilter { obj: File? -> obj!!.isFile() })
            if (fileArray == null || fileArray.size == 0) {
                throw IOException("No files found in $directory")
            }
            val files: MutableList<File> = ArrayList<File>(Arrays.asList<File?>(*fileArray))
            if (files.isEmpty()) {
                throw IOException("No files found in $directory")
            }

            // sort them
            files.sortWith(Comparator.comparing<File?, String?>(Function { obj: File? -> obj!!.getName() }))

            // make the source map
            var hiddenMap = createSourceMap(files, policy)
            if (hiddenMap.isEmpty()) {
                return false
            }

            // determine if it's already unmodifiable, wrap if not
            val map: MutableMap<*, *> = mutableMapOf<Any?, Any?>()
            if (hiddenMap.javaClass != map.javaClass) {
                hiddenMap = Collections.unmodifiableMap<IContentType<*>, ILoadableDataSource<*>>(hiddenMap)
            }
            this.fileMap = hiddenMap as Map<IContentType<*>, ILoadableDataSource<*>>?

            // do load
            try {
                when (loadPolicy) {
                    ILoadPolicy.Companion.BACKGROUND_LOAD -> load(false)
                    ILoadPolicy.Companion.IMMEDIATE_LOAD  -> load(true)
                    else                                  -> {}
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

    /*
     * (non-Javadoc)
     *
     * @see edu.edu.mit.jwi.data.ILoadable#load()
     */
    override fun load() {
        try {
            load(false)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.edu.mit.jwi.data.ILoadable#load(boolean)
     */
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
                checkNotNull(fileMap)
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
     * Creates the map that contains the content types mapped to the data
     * sources. The method should return a non-null result, but it may be empty
     * if no data sources can be created. Subclasses may override this method.
     *
     * @param files  the files from which the data sources should be created, may
     * not be `null`
     * @param policy the load policy of the provider
     * @return a map, possibly empty, but not `null`, of content
     * types mapped to data sources
     * @throws NullPointerException if the file list is `null`
     * @throws IOException          if there is a problem creating the data source
     * @since JWI 2.2.0
     */
    @NonNull
    @Throws(IOException::class)
    protected fun createSourceMap(@NonNull files: MutableList<File>, policy: Int): MutableMap<IContentType<*>?, ILoadableDataSource<*>> {
        val result: MutableMap<IContentType<*>?, ILoadableDataSource<*>> = HashMap<IContentType<*>?, ILoadableDataSource<*>>()
        for (contentType in prototypeMap.values) {
            var file: File? = null

            // give first chance to matcher
            if (sourceMatcher.containsKey(contentType.key)) {
                val regex = checkNotNull(sourceMatcher.get(contentType.key))
                file = match(regex, files)
            }

            // if it failed fall back on data types
            if (file == null) {
                val dataType: IDataType<*> = contentType.dataType
                file = find(dataType, contentType.pOS, files)
            }

            // if it failed continue
            if (file == null) {
                continue
            }

            // do not remove file from possible choices as both content types may use the same file
            if ((contentType.key != ContentTypeKey.SENSE) && (contentType.key != ContentTypeKey.SENSES) && (contentType.key != ContentTypeKey.INDEX_ADJECTIVE) && (contentType.key != ContentTypeKey.INDEX_ADVERB) && (contentType.key != ContentTypeKey.INDEX_NOUN) && (contentType.key != ContentTypeKey.INDEX_VERB)
            ) {
                files.remove(file)
            }

            result.put(contentType, createDataSource(file, contentType, policy))
            if (verbose) {
                System.out.printf("%s %s%n", contentType, file.getName())
            }
        }
        return result
    }

    @Nullable
    private fun match(@NonNull pattern: String, @NonNull files: MutableList<File>): File? {
        for (file in files) {
            val name = file.getName()
            if (name.matches(pattern.toRegex())) {
                return file
            }
        }
        return null
    }

    /**
     * Creates the actual data source implementations.
     *
     * @param <T>         the content type of the data source
     * @param file        the file from which the data source should be created, may not
     * be `null`
     * @param contentType the content type of the data source
     * @param policy      the load policy to follow when creating the data source
     * @return the created data source
     * @throws NullPointerException if any argument is `null`
     * @throws IOException          if there is an IO problem when creating the data source
     * @since JWI 2.2.0
     */
    @Throws(IOException::class)
    protected fun <T> createDataSource(file: File,  contentType: IContentType<T>, policy: Int): ILoadableDataSource<T> {
        var src: ILoadableDataSource<T>
        if (contentType.dataType === DataType.DATA) {
            src = createDirectAccess<T>(file, contentType)
            src.open()
            if (policy == ILoadPolicy.Companion.IMMEDIATE_LOAD) {
                try {
                    src.load(true)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }

            // check to see if direct access works with the file
            // often people will extract the files incorrectly on Windows machines
            // and the binary files will be corrupted with extra CRs

            // get first line
            val itr: Iterator<String?> = src.iterator()
            val firstLine = itr.next()
            if (firstLine == null) {
                return src
            }

            // extract key
            val parser = checkNotNull(contentType.dataType.parser)
            val s = checkNotNull(parser.parseLine(firstLine) as ISynset)
            val key = zeroFillOffset(s.offset)

            // try to find line by direct access
            val soughtLine = src.getLine(key)
            if (soughtLine != null) {
                return src
            }

            val pos: POS? = checkNotNull(contentType.pOS)
            System.err.println(System.currentTimeMillis().toString() + " - Error on direct access in " + pos + " data file: check CR/LF endings")
        }

        src = createBinarySearch<T>(file, contentType)
        src.open()
        if (policy == ILoadPolicy.Companion.IMMEDIATE_LOAD) {
            try {
                src.load(true)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        return src
    }

    /**
     * Creates a direct access data source for the specified type, using the
     * specified file.
     *
     * @param <T>         the parameter of the content type
     * @param file        the file on which the data source is based; may not be
     * `null`
     * @param contentType the data type for the data source; may not be
     * `null`
     * @return the data source
     * @throws NullPointerException if either argument is `null`
     * @since JWI 2.2.0
    </T> */
    @NonNull
    protected fun <T> createDirectAccess(file: File, contentType: IContentType<T>): ILoadableDataSource<T> {
        return DirectAccessWordnetFile<T>(file, contentType)
    }

    /**
     * Creates a binary search data source for the specified type, using the
     * specified file.
     *
     * @param <T>         the parameter of the content type
     * @param file        the file on which the data source is based; may not be
     * `null`
     * @param contentType the data type for the data source; may not be
     * `null`
     * @return the data source
     * @throws NullPointerException if either argument is `null`
     * @since JWI 2.2.0
    </T> */
    @NonNull
    protected fun <T> createBinarySearch(file: File, contentType: IContentType<T>): ILoadableDataSource<T> {
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
            checkNotNull(fileMap)
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
     * @since JWI 1.1
     */
    protected fun checkOpen() {
        if (!isOpen) {
            throw ObjectClosedException()
        }
    }

      override fun <T> getSource(contentType: IContentType<T>): ILoadableDataSource<T>? {
        checkOpen()

        // assume at first this the prototype
        var actualType = prototypeMap[contentType!!.key]

        // if this does not map to an adjusted type, we will check under it directly
        if (actualType == null) {
            actualType = contentType
        }
        checkNotNull(fileMap)
        return fileMap!![actualType] as ILoadableDataSource<T>
    }

    override val types: Set<IContentType<*>>
        get() {
            try {
                lifecycleLock.lock()
                return LinkedHashSet<IContentType<*>>(prototypeMap.values)
            } finally {
                lifecycleLock.unlock()
            }
        }

    /**
     * A thread class which tries to load each data source in this provider.
     *
     * @author Mark A. Finlayson
     * @version 2.4.0
     * @since JWI 2.2.0
     */
    protected inner class JWIBackgroundLoader : Thread() {

        // cancel flag
        @Transient
        private var cancel = false

        /**
         * Constructs a new background loader that operates
         * on the internal data structures of this provider.
         *
         * @since JWI 2.2.0
         */
        init {
            setName(JWIBackgroundLoader::class.java.getSimpleName())
            setDaemon(true)
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Thread#run()
         */
        override fun run() {
            try {
                checkNotNull(fileMap)
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
         *
         * @since JWI 2.2.0
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
     * Determines a version from the set of data sources, if possible, otherwise
     * returns [IVersion.NO_VERSION]
     *
     * @param srcs the data sources to be used to determine the version
     * @return the single version that describes these data sources, or
     * [IVersion.NO_VERSION] if there is none
     * @since JWI 2.1.0
     */
    @Nullable
    protected fun determineVersion(srcs: Collection<IDataSource<*>>): IVersion? {
        var ver: IVersion? = IVersion.NO_VERSION
        for (dataSrc in srcs) {
            // if no version to set, ignore
            if (dataSrc.version == null) {
                continue
            }

            // init version
            if (ver === IVersion.NO_VERSION) {
                ver = dataSrc.version
                continue
            }

            // if version different from current
            if (ver != dataSrc.version) {
                return IVersion.NO_VERSION
            }
        }
        return ver
    }

    companion object {

        var verbose: Boolean = false

        /**
         * Transforms a URL into a File. The URL must use the 'file' protocol and
         * must be in a UTF-8 compatible format as specified in
         * [URLDecoder].
         *
         * @param url url
         * @return a file pointing to the same place as the url
         * @throws NullPointerException     if the url is `null`
         * @throws IllegalArgumentException if the url does not use the 'file' protocol
         * @since JWI 1.0
         */
        @NonNull
        fun toFile(@NonNull url: URL): File {
            require(url.getProtocol() == "file") { "URL source must use 'file' protocol" }
            try {
                return File(URLDecoder.decode(url.getPath(), "UTF-8"))
            } catch (e: UnsupportedEncodingException) {
                throw RuntimeException(e)
            }
        }

        /**
         * Transforms a file into a URL.
         *
         * @param file the file to be transformed
         * @return a URL representing the file
         * @throws NullPointerException if the specified file is `null`
         * @since JWI 2.2.0
         */
        fun toURL(file: File): URL {
            val uri = URI("file", "//", file.toURI().toURL().getPath(), null)
            return URL("file", null, uri.rawPath)
        }

        /**
         * A utility method for checking whether a file represents an existing local
         * directory.
         *
         * @param url the url object to check, may not be `null`
         * @return `true` if the url object represents a local directory
         * which exists; `false` otherwise.
         * @throws NullPointerException if the specified url object is `null`
         * @since JWI 2.4.0
         */
        fun isLocalDirectory(@NonNull url: URL): Boolean {
            if (url.getProtocol() != "file") {
                return false
            }
            val file: File = toFile(url)
            return isLocalDirectory(file)
        }

        /**
         * A utility method for checking whether a file represents an existing local
         * directory.
         *
         * @param dir the file object to check, may not be `null`
         * @return `true` if the file object represents a local directory
         * which exist; `false` otherwise.
         * @throws NullPointerException if the specified file object is `null`
         * @since JWI 2.4.0
         */
        fun isLocalDirectory(@NonNull dir: File): Boolean {
            return dir.exists() && dir.isDirectory()
        }
    }
}