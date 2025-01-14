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
 * A detector for comment lines in data resources.
 * Also serve as comparators that say how comment lines are ordered, if at all.
 *
 * Default comment detector that is designed for comments found at the head of Wordnet dictionary files.
 * It assumes that each comment line starts with two spaces, followed by a number that indicates the position of the comment line relative to the rest of the comment lines in the file.
 */
object CommentProcessor : Comparator<String> {

    override fun compare(s1: String, s2: String): Int {
        var s1 = s1.trim { it <= ' ' }
        var s2 = s2.trim { it <= ' ' }

        var idx1 = s1.indexOf(' ')
        if (idx1 == -1) {
            idx1 = s1.length
        }
        var idx2 = s2.indexOf(' ')
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

    /**
     * Whether the specified string is a comment line
     *
     * @param line the line to be analyzed
     * @return true if the specified string is a comment line, false otherwise.
     */
    fun isCommentLine(line: String): Boolean {
        return line.length >= 2 && line[0] == ' ' && line[1] == ' '
    }
}
