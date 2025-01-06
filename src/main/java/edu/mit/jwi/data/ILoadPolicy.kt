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
 * An object with a load policy. Usually objects that implement this interface
 * also implement the [ILoadable] interface, but not always. A load policy
 * specifies what happens when the object is instantiated or initialized.
 * Load policies are implemented as bit flags rather than enum objects (or
 * something else) to allow policies to indicate a number of possible
 * permitted loading behaviors.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.2.0
 */
interface ILoadPolicy {

    /**
     * Returns the load policy for this object, expressed as an integer.
     *
     * @return the load policy for this object
     * @since JWI 2.2.0
     */
    /**
     * Sets the load policy for this object. If the object is currently loaded,
     * or in the process of loading, the load policy will not take effect until
     * the next time object is instantiated, initialized, or opened.
     *
     * @param loadPolicy the policy to implement; may be one of `NO_LOAD`,
     * `BACKGROUND_LOAD`, `IMMEDIATE_LOAD` or
     * an implementation-dependent value.
     * @since JWI 2.2.0
     */
    var loadPolicy: Int

    companion object {

        /**
         * Loading behavior where the object does not load itself when instantiated,
         * initialized, or opened. Loading can be initiated through other means (e.g., a
         * call to the [ILoadable.load] method, if the object
         * supports it). Value is 1 &lt;&lt; 1.
         *
         * @since JWI 2.2.0
         */
        const val NO_LOAD: Int = 1 shl 1

        /**
         * Loading behavior where the object loads itself in the background when
         * instantiated, initialized, or opened. Value is 1 &lt;&lt; 2.
         *
         * @since JWI 2.2.0
         */
        const val BACKGROUND_LOAD: Int = 1 shl 2

        /**
         * Loading behavior where the object loads itself when instantiated,
         * initialized, or opened, blocking the method. Value is 1 &lt;&lt; 3.
         *
         * @since JWI 2.2.0
         */
        const val IMMEDIATE_LOAD: Int = 1 shl 3
    }
}
