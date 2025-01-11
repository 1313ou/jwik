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

import java.io.Serializable

/**
 * A description of a Wordnet lexical file. This interface does not give access to the actual lexicographer's file,
 * but rather is a description, giving the name of the file, it's assigned number, and a brief description.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.1.0
 */
interface ILexFile : IHasPOS, Serializable {

    /**
     * The number of the lexicographer file.
     * This is used in sense keys and the data files.
     * A lexical file number is always in the closed range , between 0 and 99, inclusive [0, 99].
     * @since JWI 2.1.0
     */
    val number: Int

    /**
     * Returns the name of the lexicographer file. The string will not be empty or all whitespace.
     * @since JWI 2.1.0
     */
    val name: String

    /**
     * Returns a description of the lexicographer file contents.
     * The string will not be empty or all whitespace.
     * @since JWI 2.1.0
     */
    val description: String
}
