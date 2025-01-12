/* ******************************************************************************
 * Java Wordnet Interface Library (JWI) v2.4.0
 * Copyright (c) 2007-2015 Mark A. Finlayson
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 4.0
 * International Public License, which means it may be freely used for all
 * purposes, as long as proper acknowledgment is made.  See the license file
 * included with this distribution for more details.
 *******************************************************************************/
package edu.mit.jwi.data.compare

/**
 *
 *
 * A comparator that captures the ordering of lines in sense index files (e.g.,
 * the `sense.index` file). These files are ordered alphabetically by
 * sense key.
 *
 *
 *
 * This class follows a singleton design pattern, and is not intended to be
 * instantiated directly; rather, call the [.getInstance] method to get
 * the singleton instance.
 *
 * This constructor is marked protected so that the class may be
 * sub-classed, but not directly instantiated. Obtain instances of this
 * class via the static [.getInstance] method.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.1.0
 */
open class SenseKeyLineComparator protected constructor() : ILineComparator {

    override fun compare(line1: String, line2: String): Int {
        // get sense keys
        var line1 = line1
        var line2 = line2
        val i1 = line1.indexOf(' ')
        val i2 = line2.indexOf(' ')
        line1 = if (i1 == -1) line1 else line1.substring(0, i1)
        line2 = if (i2 == -1) line2 else line2.substring(0, i2)
        return compareSenseKeys(line1, line2)
    }

    /**
     * Compare senseKeys (overridable if non-standard compare is needed)
     *
     * @param senseKey1 sense key 1
     * @param senseKey2 sense key 1
     * @return compare code
     */
    protected open fun compareSenseKeys(senseKey1: String, senseKey2: String): Int {
        return senseKey1.compareTo(senseKey2, ignoreCase = true)
    }

    override val commentDetector: CommentProcessor?
        get() = null

    companion object {

        /**
         * Returns the singleton instance of this class, instantiating it if
         * necessary. The singleton instance will not be null.
         *
         * @return the non-null singleton instance of this class,
         * instantiating it if necessary.
         * @since JWI 2.1.0
         */
        var instance: SenseKeyLineComparator? = null
            get() {
                if (field == null) {
                    field = SenseKeyLineComparator()
                }
                return field
            }
            private set
    }
}
