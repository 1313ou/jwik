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
 * An object that can be loaded. What 'loading' means may be implementation dependent, but usually it will mean 'loaded into memory'.
 */
interface ILoadable {

    /**
     * Starts a simple, non-blocking load. If the object is already loaded, the method returns immediately and has no effect.
     * If the object is in the process of loading, the method also returns immediately.
     */
    fun load()

    /**
     * Initiates the loading process. Depending on the flag, the method may return immediately (`block` is false), or return only when the loading process is complete.
     * If the object is already loaded, the method returns immediately and has no effect.
     * If the object is in the process of loading, and the method is called in blocking mode, the method blocks until loading is complete, even if that call of the method did not initiate the loading process.
     * Some implementors of this interface may not support the immediate-return functionality.
     *
     * @param block if true, the method returns only when the loading process is complete; if false, the method returns immediately.
     * @throws InterruptedException if the method is blocking, and is interrupted while waiting for loading to complete
     */
    @Throws(InterruptedException::class)
    fun load(block: Boolean)

    /**
     * Returns whether this object is loaded or not.
     * This method should return true only if the loading process has completed and the object is actually loaded; if the object is still in the process of loading, or failed to load, the method should return false.
     *
     * @return true if the method has completed loading; false otherwise
     */
    val isLoaded: Boolean
}
