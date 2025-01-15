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
 * The data that can be obtained from a line in an exception entry file. Because
 * each exception entry does not specify its associated part of speech, this object
 * is just a proxy and must be supplemented by the part of speech at some
 * point to make a full `IExceptionEntry` object.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
open class ExceptionEntryProxy : Serializable {

    var surfaceForm: String

    var rootForms: List<String>

    /**
     * Constructs a new proxy that is a copy of the specified proxy
     *
     * @param proxy the proxy to be copied
     */
    constructor(proxy: ExceptionEntryProxy) {
        this.surfaceForm = proxy.surfaceForm
        this.rootForms = proxy.rootForms
    }

    /**
     * Constructs a new proxy with the specified field values.
     *
     * @param surfaceForm the surface form for the entry; may not be null, empty, or all whitespace
     * @param rootForms   the root forms for the entry; may not contain null, empty, or all whitespace strings
     */
    constructor(surfaceForm: String, rootForms: Array<String>) {
        this.surfaceForm = surfaceForm
        this.rootForms = rootForms
            .map { it.trim { it <= ' ' } }
            .also { it.isNotEmpty() }
            .toList()
    }

    override fun toString(): String {
        return "EXC-$surfaceForm[${rootForms.joinToString(separator = ", ")}]"
    }
}
