/* ******************************************************************************
 * Java Wordnet Interface Library (JWI) v2.4.0
 * Copyright (c) 2007-2015 Mark A. Finlayson
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 4.0
 * International Public License, which means it may be freely used for all
 * purposes, as long as proper acknowledgment is made.  See the license file
 * included with this distribution for more details.
 *******************************************************************************/
package edu.mit.jwi.item

import edu.mit.jwi.data.IContentType
import edu.mit.jwi.data.IDataType
import edu.mit.jwi.data.WordnetFile.Companion.getLine
import edu.mit.jwi.data.compare.ILineComparator
import java.io.Serializable
import java.nio.ByteBuffer
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Default, concrete implementation of the [IVersion] interface. This
 * class, much like the [Integer] class, caches instances, which should be
 * created via the `createVersion` methods.
 *
 * This version object takes an optional bugfix version number and string qualifier.
 * The qualifier may only contain characters are that are valid Java
 *
 * Creates a new version object with the specified version numbers.
 *
 * Clients should normally obtain instances of this class via the static
 * `getVersion` methods.
 *
 * @param majorVersion     the major version number, i.e., the '1' in 1.2.3.q
 * @param minorVersion     the minor version number, i.e., the '2' in 1.2.3.q
 * @param bugfixVersion    the bugfix version number, i.e., the '3' in 1.2.3.q
 * @param qualifier        the version qualifier, i.e., the 'q' in 1.2.3.q
 * @throws IllegalArgumentException if any of the version numbers are negative, or the qualifier
 * is not a legal qualifier
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.1.0
 */
open class Version(

    /**
     * The major version number, i.e., the '1' in '1.7.2'.
     * Never negative
     */
    open val majorVersion: Int,

    /**
     * The minor version number, i.e., the '7' in '1.7.2'.
     * Never negative
     */
    open val minorVersion: Int,

    /**
     * Bugfix version number, i.e., the '2' in '1.7.2'.
     * Never negative
     */
    open val bugfixVersion: Int,

    qualifier0: String,

    ) : Serializable {

    /**
     * The version qualifier, i.e., the 'abc' in '1.7.2.abc'. The qualifier is never null, but may be empty.
     */
    open val qualifier: String? = checkVersion(majorVersion, minorVersion, bugfixVersion, qualifier0)

    @Transient
    private var toString: String? = null

    override fun hashCode(): Int {
        return hashCode(majorVersion, minorVersion, bugfixVersion, qualifier)
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (obj !is Version) {
            return false
        }
        val other = obj
        if (majorVersion != other.majorVersion) {
            return false
        }
        if (minorVersion != other.minorVersion) {
            return false
        }
        if (bugfixVersion != other.bugfixVersion) {
            return false
        }
        return qualifier == other.qualifier
    }

    override fun toString(): String {
        if (toString == null) {
            toString = makeVersionString(this.majorVersion, this.minorVersion, this.bugfixVersion, qualifier)
        }
        return toString!!
    }

    /**
     * This utility method implements the appropriate deserialization for this object.
     *
     * @return the appropriate deserialized object.
     * @since JWI 2.4.0
     */
    private fun readResolve(): Any {
        return getVersion(majorVersion, minorVersion, bugfixVersion, qualifier)
    }

    companion object {

        // only create one instance of any version
        private val versionCache: MutableMap<Int?, Version?> = HashMap<Int?, Version?>()

        // Princeton Wordnet versions
        val ver16: Version = getVersion(1, 6, 0)
        val ver17: Version = getVersion(1, 7, 0)
        val ver171: Version = getVersion(1, 7, 1)
        val ver20: Version = getVersion(2, 0, 0)
        val ver21: Version = getVersion(2, 1, 0)
        val ver30: Version = getVersion(3, 0, 0)
        val ver31: Version = getVersion(3, 1, 0)

        // Stanford Augmented Wordnet versions
        val ver21swn_10k: Version = getVersion(2, 1, 0, "swn_10k")
        val ver21swn_20k: Version = getVersion(2, 1, 0, "swn_20k")
        val ver21swn_30k: Version = getVersion(2, 1, 0, "swn_30k")
        val ver21swn_40k: Version = getVersion(2, 1, 0, "swn_40k")
        val ver21swn_400k_cropped: Version = getVersion(2, 1, 0, "swn_400k_cropped")
        val ver21swn_400k_full: Version = getVersion(2, 1, 0, "swn_400k_full")

        /**
         * The byte offset of the version indicator in the standard Wordnet file headers.
         *
         * @since JWI 2.1.0
         */
        const val versionOffset: Int = 803

        /**
         * Checks the supplied version numbers. Throws an
         * [IllegalArgumentException] if they do not define a legal version.,
         *
         * @param major     the major version number
         * @param minor     the minor version number
         * @param bugfix    the bugfix version number
         * @param qualifier the qualifier to check
         * @return the null-masked qualifier
         * @throws IllegalArgumentException if the supplied arguments do not identify a legal version
         * @since JWI 2.2.0
         */

        fun checkVersion(major: Int, minor: Int, bugfix: Int, qualifier: String?): String {
            checkVersionNumber(major, minor, bugfix)
            return checkQualifier(qualifier)
        }

        /**
         * Checks the supplied version numbers. Throws an
         * [IllegalArgumentException] if the version numbers are not valid
         * (that is, any are below zero).
         *
         * @param major  the major version number
         * @param minor  the minor version number
         * @param bugfix the bugfix version number
         * @throws IllegalArgumentException if any of the supplied numbers are negative
         * @since JWI 2.1.0
         */
        fun checkVersionNumber(major: Int, minor: Int, bugfix: Int) {
            require(!isIllegalVersionNumber(major, minor, bugfix)) { "Illegal version number: " + makeVersionString(major, minor, bugfix, null) }
        }

        /**
         * Checks the specified qualifier for legality. Throws an
         * [IllegalArgumentException] if it is not a legal qualifier.
         *
         * @param qualifier the qualifier to check
         * @return the null-masked qualifier
         * @see .isIllegalQualifier
         * @since JWI 2.1.0
         */

        fun checkQualifier(qualifier: String?): String {
            if (qualifier == null) {
                return ""
            }
            require(!isIllegalQualifier(qualifier)) { "Illegal version qualifier: $qualifier" }
            return qualifier
        }

        /**
         * Returns true if the arguments identify a legal version; false otherwise.
         *
         * @param major     the major version number
         * @param minor     the minor version number
         * @param bugfix    the bugfix version number
         * @param qualifier the version qualifier
         * @return true if the arguments identify a legal version;
         * false otherwise.
         * @since JWI 2.1.0
         */
        fun isIllegalVersion(major: Int, minor: Int, bugfix: Int, qualifier: String): Boolean {
            if (isIllegalVersionNumber(major, minor, bugfix)) {
                return true
            }
            return isIllegalQualifier(qualifier)
        }

        /**
         * Returns true if any of three numbers are negative (unless specific NO_VERSION)
         *
         * @param major  the major version number
         * @param minor  the minor version number
         * @param bugfix the bugfix version number
         * @return true if all the numbers are non-negative;
         * false otherwise
         * @since JWI 2.1.0
         */
        fun isIllegalVersionNumber(major: Int, minor: Int, bugfix: Int): Boolean {
            if (major == -1 && minor == -1 && bugfix == -1) return false
            if (major < 0)
                return true
            if (minor < 0)
                return true
            return bugfix < 0
        }

        /**
         * Returns false if the specified qualifier is legal, namely, if
         * the string is either the empty string, or contains only characters that
         * are found in valid java identifiers.
         *
         * @param qualifier the qualifier to check
         * @return true if not a legal qualifier; false
         * otherwise
         * @throws NullPointerException if the specified string is null
         * @see Character.isJavaIdentifierPart
         * @since JWI 2.2.0
         */
        fun isIllegalQualifier(qualifier: String): Boolean {
            var c: Char
            for (i in 0..<qualifier.length) {
                c = qualifier[i]
                if (Character.isLetterOrDigit(c)) {
                    continue
                }
                if (c == '_' || c == '-') {
                    continue
                }
                return true
            }
            return false
        }

        /**
         * Creates and caches, or retrieves from the cache, a version object
         * corresponding to the specified numbers.
         *
         * @param major  the major version number
         * @param minor  the minor version number
         * @param bugfix the bugfix version number
         * @return the cached version object corresponding to these numbers
         * @since JWI 2.1.0
         */

        fun getVersion(major: Int, minor: Int, bugfix: Int): Version {
            return getVersion(major, minor, bugfix, null)
        }

        /**
         * Creates and caches, or retrieves from the cache, a version object
         * corresponding to the specified numbers.
         *
         * @param major     the major version number
         * @param minor     the minor version number
         * @param bugfix    the bugfix version number
         * @param qualifier the version qualifier
         * @return the cached version object corresponding to these numbers
         * @throws IllegalArgumentException if the version numbers and qualifier are not legal
         * @since JWI 2.2.0
         */

        fun getVersion(major: Int, minor: Int, bugfix: Int, qualifier: String?): Version {
            var qualifier = qualifier
            qualifier = checkVersion(major, minor, bugfix, qualifier)
            val hash = hashCode(major, minor, bugfix, qualifier)
            var version: Version? = versionCache[hash]
            if (version == null) {
                version = Version(major, minor, bugfix, qualifier)
                versionCache.put(version.hashCode(), version)
            }
            return version
        }

        // fields for version parsing
        private val periodPattern: Pattern = Pattern.compile("\\Q.\\E")
        private val digitPattern: Pattern = Pattern.compile("\\d+")
        private const val wordnetStr = "WordNet"
        private const val copyrightStr = "Copyright"
        private val versionPattern: Pattern = Pattern.compile("WordNet\\s+\\d+\\Q.\\E\\d+(\\Q.\\E\\d+)?\\s+Copyright")

        /**
         * Creates a version string for the specified version numbers.  If a version's
         * bugfix number is 0, and if the qualifier is null or empty, the string produced is of the form "x.y".  I
         *
         * @param major     the major version number, i.e., the '1' in 1.2.3.q
         * @param minor     the minor version number, i.e., the '2' in 1.2.3.q
         * @param bugfix    the bugfix version number, i.e., the '3' in 1.2.3.q
         * @param qualifier the version qualifier, i.e., the 'q' in 1.2.3.q
         * @return a string representing the specified version
         * @throws IllegalArgumentException if illegal argument
         * @since JWI 2.2.0
         */

        fun makeVersionString(major: Int, minor: Int, bugfix: Int, qualifier: String?): String {
            var qualifier = qualifier
            qualifier = checkQualifier(qualifier)
            val hasQualifier = qualifier.isNotEmpty()
            val sb = StringBuilder()
            sb.append(major)
            sb.append('.')
            sb.append(minor)
            if (bugfix > 0 || hasQualifier) {
                sb.append('.')
                sb.append(bugfix)
            }
            if (hasQualifier) {
                sb.append('.')
                sb.append(qualifier)
            }
            return sb.toString()
        }

        /**
         * Calculates the hash code for a version object with the specified version
         * numbers.
         *
         * @param major     the major version number, i.e., the '1' in 1.2.3.q
         * @param minor     the minor version number, i.e., the '2' in 1.2.3.q
         * @param bugfix    the bugfix version number, i.e., the '3' in 1.2.3.q
         * @param qualifier the version qualifier, i.e., the 'q' in 1.2.3.q
         * @return the hash code for the specified version
         * @throws IllegalArgumentException if the specified parameters do not identify a legal version
         */
        fun hashCode(major: Int, minor: Int, bugfix: Int, qualifier: String?): Int {
            var qualifier = qualifier
            qualifier = checkVersion(major, minor, bugfix, qualifier)
            val prime = 31
            var result = 1
            result = prime * result + major
            result = prime * result + minor
            result = prime * result + bugfix
            result = prime * result + qualifier.hashCode()
            return result
        }

        /**
         * Extracts a version object from a byte buffer that contains data with the
         * specified content type. If no version can be extracted, returns
         * null.
         *
         * @param contentType the content type of the data in the buffer
         * @param buffer      the buffer containing the data
         * @return the Version that was extracted, or null if none
         * @since JWI 2.1.0
         */

        fun extractVersion(contentType: IContentType<*>, buffer: ByteBuffer): Version? {
            val dataType: IDataType<*> = contentType.dataType
            if (!dataType.hasVersion()) {
                return null
            }

            // first try direct access
            var c: Char
            val sb = StringBuilder()
            for (i in versionOffset..<buffer.limit()) {
                c = Char(buffer.get(i).toUShort())
                if (Character.isWhitespace(c)) {
                    break
                }
                sb.append(c)
            }
            val version: Version? = parseVersionProtected(sb)
            if (version != null) {
                return version
            }

            // if direct access doesn't work, try walking forward in file
            // until we find a string that looks like "WordNet 2.1 Copyright"
            val cd = Objects.requireNonNull<ILineComparator?>(contentType.lineComparator).commentDetector
            if (cd == null) {
                return null
            }

            val origPos = buffer.position()

            var line: String? = null
            var m: Matcher?
            while (buffer.position() < buffer.limit()) {
                line = getLine(buffer)
                if (line == null || !cd.isCommentLine(line)) {
                    line = null
                    break
                }
                m = versionPattern.matcher(line)
                if (m.find()) {
                    line = m.group()
                    val start: Int = wordnetStr.length
                    val end: Int = line.length - copyrightStr.length
                    line = line.substring(start, end)
                    break
                }
            }
            buffer.position(origPos)
            return parseVersionProtected(line)
        }

        /**
         * Tries to transform the specified character sequence into a version
         * object. If it cannot, returns null
         *
         * @param verStr the sequence of characters to be transformed
         * @return the version, or null if the character sequence is
         * not a valid version
         * @since JWI 2.1.0
         */

        fun parseVersionProtected(verStr: CharSequence?): Version? {
            if (verStr == null) {
                return null
            }
            val parts: Array<String?> = periodPattern.split(verStr)

            if (parts.size < 2 || parts.size > 4) {
                return null
            }

            val majorStr = parts[0]!!.trim { it <= ' ' }
            if (!digitPattern.matcher(majorStr).matches()) {
                return null
            }
            val major = majorStr.toInt()

            val minorStr = parts[1]!!.trim { it <= ' ' }
            if (!digitPattern.matcher(minorStr).matches()) {
                return null
            }
            val minor = minorStr.toInt()

            var bugfix = 0
            if (parts.size >= 3) {
                val bugfixStr = parts[2]!!.trim { it <= ' ' }
                if (!digitPattern.matcher(bugfixStr).matches()) {
                    return null
                }
                bugfix = bugfixStr.toInt()
            }

            if (isIllegalVersionNumber(major, minor, bugfix)) {
                return null
            }

            var qualifier: String? = null
            if (parts.size == 4) {
                qualifier = parts[3]!!.trim { it <= ' ' }
                if (isIllegalQualifier(qualifier)) {
                    return null
                }
            }
            return getVersion(major, minor, bugfix, qualifier)
        }

        /**
         * Tries to transform the specified character sequence into a version
         * object.
         *
         * @param verStr the sequence of characters to be transformed
         * @return the version
         * @throws NullPointerException     if the character sequence is null
         * @throws IllegalArgumentException if the character sequence does not correspond to a legal
         * version
         * @since JWI 2.1.0
         */
        fun parseVersion(verStr: CharSequence): Version {

            val parts: Array<String?> = periodPattern.split(verStr)
            require(!(parts.size < 2 || parts.size > 4))

            // parts
            val major = parts[0]!!.trim { it <= ' ' }.toInt()
            val minor = parts[1]!!.trim { it <= ' ' }.toInt()
            val bugfix = if (parts.size < 3) 0 else parts[2]!!.trim { it <= ' ' }.toInt()
            val qualifier = if (parts.size < 4) null else parts[3]!!.trim { it <= ' ' }
            return getVersion(major, minor, bugfix, qualifier)
        }

        // internal cache of declared version
        private var versions: List<Version>

        init {
            val l = listOf(
                ver16,
                ver17,
                ver171,
                ver20,
                ver21,
                ver30,
                ver31,
                ver21swn_10k,
                ver21swn_20k,
                ver21swn_30k,
                ver21swn_40k,
                ver21swn_400k_cropped,
                ver21swn_400k_full,
            )

            // make the value set unmodifiable
            versions = Collections.unmodifiableList<Version?>(l)
        }

        /**
         * Emulates the Enum.values() function.
         *
         * @return all the static data type instances listed in the class, in the
         * order they are declared.
         * @since JWI 2.0.0
         */
        fun values(): List<Version?> {
            return versions
        }

        /**
         * A dummy version object used to indicate that the version has been calculated, and determined to be null.
         */
        @JvmField
        val NO_VERSION: Version = Version(-1, -1, -1, "unversioned")
    }
}
