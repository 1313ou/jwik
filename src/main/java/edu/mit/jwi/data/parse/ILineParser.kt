/* ******************************************************************************
 * Java Wordnet Interface Library (JWI) v2.4.0
 * Copyright (c) 2007-2015 Mark A. Finlayson
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 4.0
 * International Public License, which means it may be freely used for all
 * purposes, as long as proper acknowledgment is made.  See the license file
 * included with this distribution for more details.
 *******************************************************************************/
package edu.mit.jwi.data.parse

/**
 * A parser that transforms lines of data from a data source into data objects.
 *
 * @param <T> the type of the object into which this parser transforms lines
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
</T> */
interface ILineParser<T> {

    /**
     * Given the line of data, this method produces an object of class
     * `T`.
     *
     * @param line the line to be parsed
     * @return the object resulting from the parse
     * @throws NullPointerException      if the specified line is `null`
     * @throws MisformattedLineException if the line is malformed in some way
     * @since JWI 1.0
     */
    fun parseLine(line: String): T
}

/**
 * Thrown when a line from a data resource does not match expected formatting
 * conventions.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
class MisformattedLineException : RuntimeException {
    /**
     * Constructs a new exception with `null` as its detail message. The
     * cause is not initialized, and may subsequently be initialized by a call
     * to [.initCause].
     *
     * @since JWI 2.0.0
     */
    constructor() : super()
    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized, and may subsequently be initialized by a call to
     * [.initCause].
     *
     * @param message the detail message. The detail message is saved for later
     * retrieval by the [.getMessage] method.
     * @since JWI 1.0
     */
    constructor(message: String) : super(message)
    /**
     *
     *
     * Constructs a new exception with the specified detail message and cause.
     *
     * Note that the detail message associated with `cause` is
     * *not* automatically incorporated in this runtime exception's detail
     * message.
     *
     * @param message the detail message (which is saved for later retrieval by the
     * [.getMessage] method).
     * @param cause   the cause (which is saved for later retrieval by the
     * [.getCause] method). (A `null` value is
     * permitted, and indicates that the cause is nonexistent or
     * unknown.)
     * @since JWI 1.0
     */
    constructor(message: String, cause: Throwable?) : super(message, cause)
    /**
     * Constructs a new exception with the specified cause and a detail message
     * of `(cause==null ? null : cause.toString())` (which typically
     * contains the class and detail message of `cause`). This constructor
     * is useful for runtime exceptions that are little more than wrappers for
     * other throwables.
     *
     * @param cause the cause (which is saved for later retrieval by the
     * [.getCause] method). (A `null` value is
     * permitted, and indicates that the cause is nonexistent or
     * unknown.)
     * @since JWI 2.0.0
     */
    constructor(cause: Throwable?) : super(cause)
}
