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
 * Default implementation of the `ISynsetID` interface
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 1.0
 */
class SynsetID(offset: Int, pos: POS) : ISynsetID {

    override val offset: Int

    override val pOS: POS

    /**
     * Constructs a new synset id with the specified offset and part of speech.
     *
     * @param offset the offset
     * @param pos    the part of speech; may not be `null`
     * @throws NullPointerException     if the specified part of speech is `null`
     * @throws IllegalArgumentException if the specified offset is not a legal offset
     * @since JWI 1.0
     */
    init {
        if (pos == null) {
            throw NullPointerException()
        }
        Synset.checkOffset(offset)

        this.offset = offset
        this.pOS = pos
    }

    /*
    * (non-Javadoc)
    *
    * @see java.lang.Object#hashCode()
    */
    override fun hashCode(): Int {
        val PRIME = 31
        var result = 1
        result = PRIME * result + offset
        checkNotNull(this.pOS)
        result = PRIME * result + pOS.hashCode()
        return result
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (obj !is ISynsetID) {
            return false
        }
        val other = obj
        if (offset != other.offset) {
            return false
        }
        checkNotNull(this.pOS)
        return this.pOS == other.pOS
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    
    override fun toString(): String {
        checkNotNull(this.pOS)
        return synsetIDPrefix + Synset.zeroFillOffset(offset) + '-' + pOS.tag.uppercaseChar()
    }

    companion object {

        /**
         * Generated serial version id.
         *
         * @since JWI 2.2.5
         */
        private val serialVersionUID = -6965271039816443145L

        /**
         * String prefix for the [.toString] method.
         *
         * @since JWI 2.0.0
         */
        const val synsetIDPrefix: String = "SID-"

        /**
         * Convenience method for transforming the result of the [.toString]
         * method back into an `ISynsetID`. Synset IDs are always 14
         * characters long and have the following format: SID-########-C, where
         * ######## is the zero-filled eight decimal digit offset of the synset, and
         * C is the upper-case character code indicating the part of speech.
         *
         * @param value the string representation of the id; may include leading or
         * trailing whitespace
         * @return a synset id object corresponding to the specified string
         * representation
         * @throws NullPointerException     if the specified string is `null`
         * @throws IllegalArgumentException if the specified string is not a properly formatted synset id
         * @since JWI 1.0
         */
        
        fun parseSynsetID(value: String): SynsetID {
            var value = value
            if (value == null) {
                throw NullPointerException()
            }

            value = value.trim { it <= ' ' }
            require(value.length == 14)

            require(value.startsWith("SID-"))

            // get offset
            val offset = value.substring(4, 12).toInt()

            // get pos
            val tag = value.get(13).lowercaseChar()
            val pos = POS.getPartOfSpeech(tag)
            requireNotNull(pos) { "unknown part of speech tag: $tag" }
            return SynsetID(offset, pos)
        }
    }
}
