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

import java.util.*

/**
 * Default implementation `IExceptionEntryProxy`l
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
open class ExceptionEntryProxy : IExceptionEntryProxy {

    override lateinit var surfaceForm: String
    
    override lateinit var rootForms: List<String>

    /**
     * Constructs a new proxy that is a copy of the specified proxy
     *
     * @param proxy the proxy to be copied
     * @throws NullPointerException if the specified proxy is `null`
     * @since JWI 1.0
     */
    constructor( proxy: IExceptionEntryProxy) {
        if (proxy == null) {
            throw NullPointerException()
        }
        this.surfaceForm = proxy.surfaceForm
        this.rootForms = proxy.rootForms
    }

    /**
     * Constructs a new proxy with the specified field values.
     *
     * @param surfaceForm the surface form for the entry; may not be `null`, empty, or all whitespace
     * @param rootForms   the root forms for the entry; may not contain `null`, empty, or all whitespace strings
     * @since JWI 1.0
     */
    constructor( surfaceForm: String, rootForms: Array<String>) {
        if (surfaceForm == null) {
            throw NullPointerException()
        }
        for (i in rootForms.indices) {
            if (rootForms[i] == null) {
                throw NullPointerException()
            }
            rootForms[i] = rootForms[i].trim { it <= ' ' }
            require(rootForms[i].isNotEmpty())
        }

        this.surfaceForm = surfaceForm
        this.rootForms = Collections.unmodifiableList<String>(listOf<String>(*rootForms))
    }

    /*
    * (non-Javadoc)
    *
    * @see java.lang.Object#toString()
    */
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("EXC-")
        sb.append(surfaceForm)
        sb.append('[')
        val i: Iterator<String> = rootForms.iterator()
        while (i.hasNext()) {
            sb.append(i.next())
            if (i.hasNext()) {
                sb.append(',')
            }
        }
        sb.append(']')
        return sb.toString()
    }
}
