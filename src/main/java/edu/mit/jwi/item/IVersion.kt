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
 * A Wordnet version.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.1.0
 */
interface IVersion : Serializable {

    /**
     * Returns the major version number, i.e., the '1' in '1.7.2'.
     *
     * @return the major version number, never negative
     * @since JWI 2.1.0
     */
    val majorVersion: Int

    /**
     * Returns the minor version number, i.e., the '7' in '1.7.2'.
     *
     * @return the minor version number, never negative
     * @since JWI 2.1.0
     */
    val minorVersion: Int

    /**
     * Returns the bugfix version number, i.e., the '2' in '1.7.2'.
     *
     * @return the bugfix version number, never negative
     * @since JWI 2.1.0
     */
    val bugfixVersion: Int

    /**
     * Returns the version qualifier, i.e., the 'abc' in '1.7.2.abc'. The
     * qualifier is never `null`, but may be empty.
     *
     * @return the version qualifier, non-`null`, potentially empty
     * @since JWI 2.2.0
     */
    val qualifier: String

    companion object {

        /**
         * A dummy version object used to indicate that the version has been
         * calculated, and determined to be `null`.
         *
         * @since JWI 2.2.0
         */
        @JvmField
        val NO_VERSION: IVersion = object : IVersion {
            /**
             * This serial version UID identifies the last version of JWI whose
             * serialized instances of the NO_VERSION instance are compatible with this
             * implementation.
             *
             * @since JWI 2.4.0
             */
            private val serialVersionUID: Long = 240

            override val bugfixVersion: Int
                get() {
                    throw UnsupportedOperationException()
                }

            override val majorVersion: Int
                get() {
                    throw UnsupportedOperationException()
                }

            override val minorVersion: Int
                get() {
                    throw UnsupportedOperationException()
                }

            override val qualifier: String
                get() {
                    throw UnsupportedOperationException()
                }

            /**
             * Deserialization implementation. When deserializing this object, make
             * sure to return the singleton object.
             *
             * @return the singleton dummy version object.
             * @since JWI 2.4.0
             */
            private fun readResolve(): Any {
                return NO_VERSION
            }
        }
    }
}
