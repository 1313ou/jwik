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
interface IExceptionEntryProxy : Serializable {

    val surfaceForm: String

    /**
     * Returns an unmodifiable list of acceptable root forms for the surface
     * form.
     *
     * @return A non-null, non-empty, unmodifiable list of root forms
     * @since JWI 2.0.0
     */
    val rootForms: List<String>
}
