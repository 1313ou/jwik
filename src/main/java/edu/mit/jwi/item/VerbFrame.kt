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

import java.lang.reflect.Field
import java.util.*

/**
 * Default, hard-coded, implementation of `IVerbFrame` that does not read
 * from the actual file. This is not implemented as an `Enum` so that
 * clients can instantiate their own custom verb frame objects.
 *
 * @param number      the verb frame number
 * @param template the template representing the verb frame
 *
 * @version 2.4.0
 * @since JWI 2.1.0
 */
class VerbFrame(
    override val number: Int,
    override val template: String,
) : IVerbFrame {

    override fun instantiateTemplate(verb: String): String {
        if (verb == null) {
            throw NullPointerException()
        }
        val index = template.indexOf("----")
        if (index == -1) {
            return ""
        }
        return template.substring(0, index) + verb + template.substring(index + 5)
    }

    override fun toString(): String {
        return "[" + this.number + " : " + template + " ]"
    }

    /**
     * This utility method implements the appropriate deserialization for this
     * object.
     *
     * @return the appropriate deserialized object.
     * @since JWI 2.4.0
     */
    private fun readResolve(): Any {
        val staticFrame: VerbFrame? = getFrame(this.number)
        return staticFrame ?: this
    }

    companion object {

        // standard verb frames
        val NUM_01: VerbFrame = VerbFrame(1, "Something ----s")
        val NUM_02: VerbFrame = VerbFrame(2, "Somebody ----s")
        val NUM_03: VerbFrame = VerbFrame(3, "It is ----ing")
        val NUM_04: VerbFrame = VerbFrame(4, "Something is ----ing PP")
        val NUM_05: VerbFrame = VerbFrame(5, "Something ----s something Adjective/Noun")
        val NUM_06: VerbFrame = VerbFrame(6, "Something ----s Adjective/Noun")
        val NUM_07: VerbFrame = VerbFrame(7, "Somebody ----s Adjective")
        val NUM_08: VerbFrame = VerbFrame(8, "Somebody ----s something")
        val NUM_09: VerbFrame = VerbFrame(9, "Somebody ----s somebody")
        val NUM_10: VerbFrame = VerbFrame(10, "Something ----s somebody")
        val NUM_11: VerbFrame = VerbFrame(11, "Something ----s something")
        val NUM_12: VerbFrame = VerbFrame(12, "Something ----s to somebody")
        val NUM_13: VerbFrame = VerbFrame(13, "Somebody ----s on something")
        val NUM_14: VerbFrame = VerbFrame(14, "Somebody ----s somebody something")
        val NUM_15: VerbFrame = VerbFrame(15, "Somebody ----s something to somebody")
        val NUM_16: VerbFrame = VerbFrame(16, "Somebody ----s something from somebody")
        val NUM_17: VerbFrame = VerbFrame(17, "Somebody ----s somebody with something")
        val NUM_18: VerbFrame = VerbFrame(18, "Somebody ----s somebody of something")
        val NUM_19: VerbFrame = VerbFrame(19, "Somebody ----s something on somebody")
        val NUM_20: VerbFrame = VerbFrame(20, "Somebody ----s somebody PP")
        val NUM_21: VerbFrame = VerbFrame(21, "Somebody ----s something PP")
        val NUM_22: VerbFrame = VerbFrame(22, "Somebody ----s PP")
        val NUM_23: VerbFrame = VerbFrame(23, "Somebody's (body part) ----s")
        val NUM_24: VerbFrame = VerbFrame(24, "Somebody ----s somebody to INFINITIVE")
        val NUM_25: VerbFrame = VerbFrame(25, "Somebody ----s somebody INFINITIVE")
        val NUM_26: VerbFrame = VerbFrame(26, "Somebody ----s that CLAUSE")
        val NUM_27: VerbFrame = VerbFrame(27, "Somebody ----s to somebody")
        val NUM_28: VerbFrame = VerbFrame(28, "Somebody ----s to INFINITIVE")
        val NUM_29: VerbFrame = VerbFrame(29, "Somebody ----s whether INFINITIVE")
        val NUM_30: VerbFrame = VerbFrame(30, "Somebody ----s somebody into V-ing something")
        val NUM_31: VerbFrame = VerbFrame(31, "Somebody ----s something with something")
        val NUM_32: VerbFrame = VerbFrame(32, "Somebody ----s INFINITIVE")
        val NUM_33: VerbFrame = VerbFrame(33, "Somebody ----s VERB-ing")
        val NUM_34: VerbFrame = VerbFrame(34, "It ----s that CLAUSE")
        val NUM_35: VerbFrame = VerbFrame(35, "Something ----s INFINITIVE")
        val NUM_36: VerbFrame = VerbFrame(36, "Somebody ----s at something")
        val NUM_37: VerbFrame = VerbFrame(37, "Somebody ----s for something")
        val NUM_38: VerbFrame = VerbFrame(38, "Somebody ----s on somebody")
        val NUM_39: VerbFrame = VerbFrame(39, "Somebody ----s out of somebody")

        // verb frame cache
        private val verbFrameMap: MutableMap<Int, VerbFrame>

        init {
            // get the instance fields
            val fields = VerbFrame::class.java.getFields()
            val instanceFields: MutableList<Field> = ArrayList<Field>()
            for (field in fields) {
                if (field.genericType === VerbFrame::class.java) {
                    instanceFields.add(field)
                }
            }

            // this is our backing collection
            val hidden: MutableMap<Int, VerbFrame> = LinkedHashMap<Int, VerbFrame>(instanceFields.size)

            // get the instances
            var frame: VerbFrame?
            for (field in instanceFields) {
                try {
                    frame = field.get(null) as VerbFrame?
                    if (frame != null) {
                        hidden.put(frame.number, frame)
                    }
                } catch (_: IllegalAccessException) {
                    // ignore
                }
            }

            // make the value map unmodifiable
            verbFrameMap = Collections.unmodifiableMap<Int?, VerbFrame?>(hidden)
        }

        /**
         * This emulates the Enum.values() method, in that it returns an
         * unmodifiable collection of all the static instances declared in this
         * class, in the order they were declared.
         *
         * @return an unmodifiable collection of verb frames defined in this class
         * @since JWI 2.1.0
         */
        fun values(): Collection<VerbFrame> {
            return verbFrameMap.values
        }

        /**
         * Returns the frame indexed by the specified number defined in this class,
         * or `null` if there is
         *
         * @param number the verb frame number
         * @return the verb frame with the specified number, or `null` if
         * none
         * @since JWI 2.1.0
         */
        @JvmStatic
        fun getFrame(number: Int): VerbFrame? {
            return verbFrameMap[number]
        }
    }
}
