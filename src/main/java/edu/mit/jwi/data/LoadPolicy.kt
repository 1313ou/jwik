/* ******************************************************************************
 * Java Wordnet Interface Library (JWI) v2.4.0
 * Copyright (c) 2007-2015 Mark A. Finlayson
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 4.0
 * International Public License, which means it may be freely used for all
 * purposes, as long as proper acknowledgment is made.  See the license file
 * included with this distribution for more details.
 *******************************************************************************/
package edu.mit.jwi.data

/**
 * A load policy specifies what happens when the object is instantiated or initialized.
 * Load policies are implemented as bit flags rather than enum objects (or something else)
 * to allow policies to indicate a number of possible permitted loading behaviors.
 */
object LoadPolicy {

    /**
     * Loading behavior where the object does not load itself when instantiated, initialized, or opened.
     * Loading can be initiated through other means (e.g., a call to the [ILoadable.load] method, if the object supports it).
     * Value is 1 &lt;&lt; 1.
     */
    const val NO_LOAD: Int = 1 shl 1

    /**
     * Loading behavior where the object loads itself in the background when instantiated, initialized, or opened.
     * Value is 1 &lt;&lt; 2.
     */
    const val BACKGROUND_LOAD: Int = 1 shl 2

    /**
     * Loading behavior where the object loads itself when instantiated, initialized, or opened, blocking the method.
     * Value is 1 &lt;&lt; 3.
     */
    const val IMMEDIATE_LOAD: Int = 1 shl 3
}
