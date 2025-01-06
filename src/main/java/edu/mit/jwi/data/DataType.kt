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
import edu.mit.jwi.data.parse.*
import edu.mit.jwi.item.*
import java.io.File
import java.lang.reflect.Field
import java.util.*

/**
 * A concrete implementation of the `IDataType` interface. This class
 * provides the data types necessary for Wordnet in the form of static
 * fields. It is not implemented as an `Enum` so that clients may add
 * their own content types by instantiating this class.
 *
 * @param <T> the type of object for the content type
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.0.0
 */
class DataType<T>(
    userFriendlyName: String?,
    hasVersion: Boolean,
    parser: ILineParser<T>,
    hints: Collection<String>?
) : IDataType<T> {

    // fields set on construction
    private val name: String?

    override var resourceNameHints: Set<String>

    private val hasVersion: Boolean

    override val parser: ILineParser<T>

    /**
     * Constructs a new data type. This constructor takes the hints as a
     * varargs array.
     *
     * @param userFriendlyName a user-friendly name, for easy identification of this data
     * type; may be `null`
     * @param hasVersion       `true` if the comment header for this data type
     * usually contains a version number
     * @param parser           the line parser for transforming lines from this data type
     * into objects; may not be `null`
     * @param hints            a varargs array of resource name hints for identifying the
     * resource that contains the data. may be `null`, but
     * may not contain `null`
     * @throws NullPointerException if the specified parser is `null`
     * @since JWI 2.0.0
     */
    constructor(
        userFriendlyName: String?,
        hasVersion: Boolean,
        parser: ILineParser<T>,
        vararg hints: String
    ) : this(userFriendlyName, hasVersion, parser, if (hints == null) null else listOf<String>(*hints))

    override fun hasVersion(): Boolean {
        return hasVersion
    }

     override fun toString(): String {
        return name!!
    }

    /**
     * Constructs a new data type. This constructor takes the hints as a
     * collection.
     *
     * @param userFriendlyName a user-friendly name, for easy identification of this data
     * type; may be `null`
     * @param hasVersion       `true` if the comment header for this data type
     * usually contains a version number
     * @param parser           the line parser for transforming lines from this data type
     * into objects; may not be `null`
     * @param hints            a collection of resource name hints for identifying the
     * resource that contains the data. May be `null`, but
     * may not contain `null`
     * @throws NullPointerException if the specified parser is `null`
     * @since JWI 2.0.0
     */
    init {
        if (parser == null) {
            throw NullPointerException()
        }
        this.name = userFriendlyName
        this.parser = parser
        this.hasVersion = hasVersion
        this.resourceNameHints = if (hints == null || hints.isEmpty()) setOf<String>() else Collections.unmodifiableSet<String>(HashSet<String>(hints))
    }

    companion object {

        @JvmField
        val INDEX: DataType<IIndexWord?> = DataType<IIndexWord?>("Index", true, IndexLineParser.getInstance(), "index", "idx")

        @JvmField
        val WORD: DataType<IIndexWord?> = DataType<IIndexWord?>("Word", true, IndexLineParser.getInstance(), "index", "idx")

        @JvmField
        val DATA: DataType<ISynset?> = DataType<ISynset?>("Data", true, DataLineParser.getInstance(), "data", "dat")

        @JvmField
        val EXCEPTION: DataType<IExceptionEntryProxy?> = DataType<IExceptionEntryProxy?>("Exception", false, ExceptionLineParser.getInstance(), "exception", "exc")

        @JvmField
        val SENSE: DataType<ISenseEntry?> = DataType<ISenseEntry?>("Sense", false, SenseLineParser.getInstance(), "sense")

        @JvmField
        val SENSES: DataType<Array<ISenseEntry?>?> = DataType<Array<ISenseEntry?>?>("Senses", false, SensesLineParser.getInstance(), "sense")

        // set of all data types implemented in this class
        private var dataTypes: Set<DataType<*>>? = null

        /**
         * Emulates the Enum.values() function.
         *
         * @return all the static data type instances listed in the class, in the
         * order they are declared.
         * @since JWI 2.0.0
         */
        fun values(): Collection<DataType<*>> {
            if (dataTypes == null) {
                // get all the fields containing ContentType
                val fields = DataType::class.java.getFields()
                val instanceFields: MutableList<Field> = ArrayList<Field>()
                for (field in fields) {
                    if (field.genericType === DataType::class.java) {
                        instanceFields.add(field)
                    }
                }

                // this is the backing set
                val hidden: MutableSet<DataType<*>?> = LinkedHashSet<DataType<*>?>(instanceFields.size)

                // fill in the backing set
                var dataType: DataType<*>?
                for (field in instanceFields) {
                    try {
                        dataType = field.get(null) as DataType<*>?
                        if (dataType != null) {
                            hidden.add(dataType)
                        }
                    } catch (_: IllegalAccessException) {
                        // ignore
                    }
                }

                // make the value set unmodifiable
                dataTypes = Collections.unmodifiableSet<DataType<*>?>(hidden)
            }
            return dataTypes!!
        }

        /**
         * Finds the first file that satisfies the naming constraints of both
         * the data type and part of speech. Behaviour modified.
         *
         * @param dataType the data type whose resource name hints should be used, may
         * not be `null`
         * @param pos      the part of speech whose resource name hints should be used,
         * may be `null`
         * @param files    the files to be searched, may be empty but not `null`
         * @return the file that matches both the pos and type naming conventions,
         * or `null` if none is found.
         * @throws NullPointerException if the data type or file collection is `null`
         * @since JWI 2.2.0
         */
        @Nullable
        fun find(@NonNull dataType: IDataType<*>, @Nullable pos: POS?, @NonNull files: MutableCollection<out File>): File? {
            val typePatterns = dataType.resourceNameHints
            val posPatterns: Set<String> = pos?.resourceNameHints ?: setOf<String>()
            if (typePatterns == null || typePatterns.isEmpty()) {
                for (file in files) {
                    val name = file.getName().lowercase(Locale.getDefault()) // added toLowerCase() as fix for Bug 017
                    if (containsOneOf(name, posPatterns)) {
                        return file
                    }
                }
            } else {
                for (typePattern in typePatterns) {
                    for (file in files) {
                        val name = file.getName().lowercase(Locale.getDefault()) // added toLowerCase() as fix for Bug 017
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
         * empty or null, returns `true`. If a pattern is found in the
         * target string, returns `true`. Otherwise, returns
         * `false`.
         *
         * @param target   the string to be searched
         * @param patterns the patterns to search for
         * @return `true` if the target contains one of the patterns;
         * `false` otherwise
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
