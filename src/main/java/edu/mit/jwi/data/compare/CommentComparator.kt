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
 * Default comment detector that is designed for comments found at the head of
 * Wordnet dictionary files. It assumes that each comment line starts with two
 * spaces, followed by a number that indicates the position of the comment line
 * relative to the rest of the comment lines in the file.
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
 * @since JWI 1.0
 */
class CommentComparator
private constructor() : Comparator<String>, ICommentDetector {

    override fun compare(s1: String, s2: String): Int {
        var s1 = s1
        var s2 = s2
        s1 = s1.trim { it <= ' ' }
        s2 = s2.trim { it <= ' ' }

        var idx1 = s1.indexOf(' ')
        var idx2 = s2.indexOf(' ')
        if (idx1 == -1) {
            idx1 = s1.length
        }
        if (idx2 == -1) {
            idx2 = s2.length
        }

        val num1 = s1.substring(0, idx1).toInt()
        val num2 = s2.substring(0, idx2).toInt()

        if (num1 < num2) {
            return -1
        } else if (num1 > num2) {
            return 1
        }
        return 0
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.edu.mit.jwi.data.compare.ICommentDetector#isCommentLine(java.lang.String)
     */
    override fun isCommentLine(line: String): Boolean {
        return line.length >= 2 && line[0] == ' ' && line[1] == ' '
    }

    companion object {

        /**
         * Returns the singleton instance of this class, instantiating it if
         * necessary. The singleton instance will not be `null`.
         *
         * @return the non-`null` singleton instance of this class,
         * instantiating it if necessary.
         * @since JWI 2.0.0
         */
        @JvmStatic
        var instance: CommentComparator? = null
            get() {
                if (field == null) {
                    field = CommentComparator()
                }
                return field
            }
            private set
    }
}
