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

/**
 * Default implementation of `IExceptionEntry`
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
class ExceptionEntry : ExceptionEntryProxy, IExceptionEntry {

    override val pOS: POS

    override val iD: IExceptionEntryID

    /**
     * Creates a new exception entry for the specified part of speech using the
     * information in the specified exception proxy object.
     *
     * @param proxy the proxy containing the information for the entry
     * @param pos   the part of speech for the entry
     * @throws NullPointerException if either argument is null
     * @since JWI 1.0
     */
    constructor(proxy: IExceptionEntryProxy, pos: POS) : super(proxy) {
        if (pos == null) {
            throw NullPointerException()
        }
        this.pOS = pos
        this.iD = ExceptionEntryID(surfaceForm, pos)
    }

    /**
     * Creates a new exception entry for the specified part of speech using the
     * specified surface and root forms.
     *
     * @param surfaceForm the surface form for the entry
     * @param pos         the part of speech for the entry
     * @param rootForms   the root forms for the entry
     * @throws NullPointerException if either argument is null
     * @since JWI 1.0
     */
    constructor(surfaceForm: String, pos: POS, rootForms: Array<String>) : super(surfaceForm, rootForms) {
        if (pos == null) {
            throw NullPointerException()
        }
        this.iD = ExceptionEntryID(surfaceForm!!, pos)
        this.pOS = pos
    }

    /*
    * (non-Javadoc)
    *
    * @see edu.edu.mit.jwi.item.ExceptionEntryProxy#toString()
    */

    override fun toString(): String {
        checkNotNull(this.pOS)
        return super.toString() + "-" + this.pOS
    }
}
