package edu.mit.jwi.item

import edu.mit.jwi.item.Synset.Companion.checkOffset
import java.io.Serializable
import java.util.*

/**
 * A Wordnet sense entry object, represented in the Wordnet files as a line in the index.senses
 *
 * @param senseKey the sense key of the entry
 * @param offset the synset offset of the entry
 * @param senseNumber the sense number of the entry
 * @param tagCount the tag count of the entry
 */
class SenseEntry(
    /**
     * The sense key
     */
    val senseKey: SenseKey,

    /**
     * The synset offset for this sense entry, a non-negative integer.
     */
    val offset: Int,

    /**
     * Returns the sense number for the word indicated by this entry.
     * A sense number is a positive integer.
     */
    val senseNumber: Int,

    /**
     * The non-negative tag count for the sense entry.
     * A tag count is a non-negative integer that represents the number of times the sense is tagged in various semantic concordance texts.
     * A count of 0 indicates that the sense has not been semantically tagged.
     */
    val tagCount: Int,

    ) : IHasPOS, Serializable {

    override val pOS: POS
        get() {
            return senseKey.pOS
        }

    init {
        checkOffset(offset)
    }

    override fun hashCode(): Int {
        return Objects.hash(senseKey, offset, senseNumber, tagCount)
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (obj !is SenseEntry) {
            return false
        }
        val other = obj
        if (senseKey != other.senseKey) {
            return false
        }
        if (offset != other.offset) {
            return false
        }
        if (senseNumber != other.senseNumber) {
            return false
        }
        return tagCount == other.tagCount // questionable
    }

    override fun toString(): String {
        return "$senseKey -> $offset-$pOS, #$senseNumber, tagCount=$tagCount"
    }
}
