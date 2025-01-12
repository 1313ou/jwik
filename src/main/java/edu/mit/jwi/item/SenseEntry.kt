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

import edu.mit.jwi.item.Synset.Companion.checkOffset

/**
 * Concrete implementation of the `ISenseEntry` interface.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.1.0
 */
class SenseEntry(key: SenseKey, offset: Int, num: Int, count: Int) : ISenseEntry {

    override val offset: Int

    override val senseNumber: Int

    override val tagCount: Int

    override val senseKey: SenseKey

    override val pOS: POS
        get() {
            return senseKey.pOS!!
        }

    /**
     * Constructs a new sense entry object.
     *
     * @param key    the sense key of the entry
     * @param offset the synset offset of the entry
     * @param num    the sense number of the entry
     * @param count  the tag count of the entry
     * @since JWI 2.1.0
     */
    init {
        if (key == null) {
            throw NullPointerException()
        }
        checkOffset(offset)

        this.senseKey = key
        this.offset = offset
        this.senseNumber = num
        this.tagCount = count
    }
    /*
    * (non-Javadoc)
    *
    * @see java.lang.Object#hashCode()
    */
    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + this.tagCount
        checkNotNull(this.senseKey)
        result = prime * result + senseKey.hashCode()
        result = prime * result + this.senseNumber
        result = prime * result + offset
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
        if (obj !is ISenseEntry) {
            return false
        }
        val other = obj
        if (this.tagCount != other.tagCount) {
            return false
        }
        if (this.senseNumber != other.senseNumber) {
            return false
        }
        if (offset != other.offset) {
            return false
        }
        checkNotNull(this.senseKey)
        return this.senseKey == other.senseKey
    }
}
