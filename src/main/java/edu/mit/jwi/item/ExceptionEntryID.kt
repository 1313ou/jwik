package edu.mit.jwi.item

import edu.mit.jwi.item.POS.Companion.getPartOfSpeech
import java.util.*

/**
 * A unique identifier sufficient to retrieve the specified
 * exception entry from Wordnet.
 *
 * @param surfaceForm the raw surface form for the entry
 * @property surfaceForm the surface form for the entry
 * @property pOS the part of speech for the entry
 * @throws IllegalArgumentException if the surface form is empty or all whitespace
 */
class ExceptionEntryID(
    surfaceForm: String,
    override val pOS: POS,
) : IHasPOS, IItemID {

    val surfaceForm: String = surfaceForm.asSurfaceForm()

    init {
        require(surfaceForm.isNotEmpty())
    }

    override fun toString(): String {
        return "EID-$surfaceForm -${pOS.tag}"
    }

    override fun hashCode(): Int {
        return Objects.hash(surfaceForm, pOS)
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (obj !is ExceptionEntryID) {
            return false
        }
        val other = obj
        if (surfaceForm != other.surfaceForm) {
            return false
        }
        return pOS == other.pOS
    }

    companion object {

        /**
         * Convenience method for transforming the result of the toString method back into an IExceptionEntryID.
         *
         * @param value the string to parse
         * @return the derived exception entry id
         * @throws IllegalArgumentException if the specified string does not conform to an exception entry id
         */
        fun parseExceptionEntryID(value: String): ExceptionEntryID {
            require(value.startsWith("EID-"))
            require(value[value.length - 2] == '-')

            val pos = getPartOfSpeech(value[value.length - 1])
            return ExceptionEntryID(value.substring(4, value.length - 2), pos)
        }
    }
}
