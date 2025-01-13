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

import edu.mit.jwi.data.parse.*
import edu.mit.jwi.item.*
import java.io.File
import java.util.*

/**
 * Objects that represent possible types of data that occur in the dictionary data directory.
 *
 * In the standard Wordnet distributions, data types would include, but would
 * not be limited to, *Index* files, *Data* files, and
 * *Exception* files. The objects implementing this interface are then
 * paired with an [edu.mit.jwi.item.POS] instance and
 * [ILineComparator] instance to form an instance of an
 * [IContentType] class, which identifies the specific data contained in
 * the file. Note that here, 'data' refers not to an actual file, but to an
 * instance of the `IDataSource` interface that provides access to the
 * data, be it a file in the file system, a socket connection to a database, or
 * something else.
 *
 * This class provides the data types necessary for Wordnet in the form of static
 * fields. It is not implemented as an `Enum` so that clients may add
 * their own content types by instantiating this class.
 *
 * @param <T> the type of the object returned by the parser for this data type
 * @param <T> the type of object for the content type
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.0.0
 */
class DataType<T>(
    userFriendlyName: String?,
    private val hasVersion: Boolean,
    val parser: ILineParser<T>,
    hints: Collection<String>?,
) {

    // fields set on construction
    private val name: String? = userFriendlyName

    /**
    * Returns an immutable set of strings that can be used as keywords to
    * identify resources that are of this type.
    * Set strings that can be used as keywords to identify resources that
    * are of this type.
    *
    * A set of resource name fragments
    */
    var resourceNameHints: Set<String> = if (hints == null || hints.isEmpty()) setOf<String>() else Collections.unmodifiableSet<String>(HashSet<String>(hints))

    /**
     * Constructs a new data type. This constructor takes the hints as a
     * varargs array.
     *
     * @param userFriendlyName a user-friendly name, for easy identification of this data
     * type; may be null
     * @param hasVersion       true if the comment header for this data type
     * usually contains a version number
     * @param parser           the line parser for transforming lines from this data type
     * into objects; may not be null
     * @param hints            a varargs array of resource name hints for identifying the
     * resource that contains the data. may be null, but
     * may not contain null
     * @throws NullPointerException if the specified parser is null
     * @since JWI 2.0.0
     */
    constructor(
        userFriendlyName: String?,
        hasVersion: Boolean,
        parser: ILineParser<T>,
        vararg hints: String,
    ) : this(userFriendlyName, hasVersion, parser, if (hints == null) null else listOf<String>(*hints))

    /**
     * Whether this content type usually has wordnet version information encoded in its header.
     * Whether the content file that underlies this content usually has wordnet version information in its comment header
     */
    fun hasVersion(): Boolean {
        return hasVersion
    }

    override fun toString(): String {
        return name!!
    }

    companion object {

        @JvmField
        val INDEX: DataType<IndexWord> = DataType<IndexWord>("Index", true, IndexLineParser.Companion.instance!!, "index", "idx")

        @JvmField
        val WORD: DataType<IndexWord> = DataType<IndexWord>("Word", true, IndexLineParser.Companion.instance!!, "index", "idx")

        @JvmField
        val DATA: DataType<Synset> = DataType<Synset>("Data", true, DataLineParser.Companion.instance!!, "data", "dat")

        @JvmField
        val EXCEPTION: DataType<ExceptionEntryProxy> = DataType<ExceptionEntryProxy>("Exception", false, ExceptionLineParser.Companion.instance!!, "exception", "exc")

        @JvmField
        val SENSE: DataType<SenseEntry> = DataType<SenseEntry>("Sense", false, SenseLineParser.instance!!, "sense")

        @JvmField
        val SENSES: DataType<Array<SenseEntry>> = DataType<Array<SenseEntry>>("Senses", false, SensesLineParser.instance!!, "sense")

        /**
         * Set of all data types implemented in this class
         */
        private val dataTypes: Set<DataType<*>> = Collections.unmodifiableSet<DataType<*>>(
            setOf(INDEX, WORD, DATA, EXCEPTION, SENSE, SENSES)
        )

        /**
         * Emulates the Enum.values() function.
         *
         * @return all the static data type instances listed in the class, in the
         * order they are declared.
         * @since JWI 2.0.0
         */
        fun values(): Collection<DataType<*>> {
            return dataTypes
        }

        /**
         * Finds the first file that satisfies the naming constraints of both
         * the data type and part of speech. Behaviour modified.
         *
         * @param dataType the data type whose resource name hints should be used, may
         * not be null
         * @param pos      the part of speech whose resource name hints should be used,
         * may be null
         * @param files    the files to be searched, may be empty but not null
         * @return the file that matches both the pos and type naming conventions,
         * or null if none is found.
         * @throws NullPointerException if the data type or file collection is null
         * @since JWI 2.2.0
         */
        fun find(dataType: DataType<*>, pos: POS?, files: Collection<File>): File? {
            val typePatterns = dataType.resourceNameHints
            val posPatterns: Set<String> = pos?.resourceNameHints ?: setOf<String>()
            if (typePatterns == null || typePatterns.isEmpty()) {
                for (file in files) {
                    val name = file.getName().lowercase() // added lowerCase() as fix for Bug 017
                    if (containsOneOf(name, posPatterns)) {
                        return file
                    }
                }
            } else {
                for (typePattern in typePatterns) {
                    for (file in files) {
                        val name = file.getName().lowercase() // added lowerCase() as fix for Bug 017
                        if (name.contains(typePattern) && containsOneOf(name, posPatterns)) {
                            return file
                        }
                    }
                }
            }
            return null
        }

        /**
         * Checks to see if one of the string patterns specified in the set of
         * strings is found in the specified target string. If the pattern set is
         * empty or null, returns true. If a pattern is found in the
         * target string, returns true. Otherwise, returns
         * false.
         *
         * @param target   the string to be searched
         * @param patterns the patterns to search for
         * @return true if the target contains one of the patterns;
         * false otherwise
         * @since JWI 2.2.0
         */
        fun containsOneOf(target: String, patterns: Set<String>): Boolean {
            if (patterns.isEmpty()) {
                return true
            }
            for (pattern in patterns) {
                if (target.contains(pattern)) {
                    return true
                }
            }
            return false
        }
    }
}
