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

import edu.mit.jwi.data.compare.*
import edu.mit.jwi.item.*
import java.nio.charset.Charset
import java.util.*

/**
 * Objects that represent all possible types of content
 * that are contained in the dictionary data resources.
 * Each unique object of this type will correspond to a particular resource or file.
 *
 * In the standard Wordnet distributions, examples of content types would
 * include, but would not be limited to,
 * *Index*,
 * *Data*, and
 * *Exception*
 * files for each part of speech.
 *
 * This class provides the content types necessary for Wordnet in the form of static fields.
 * It is not implemented as an `Enum` so that clients may add their own content types by instantiating this class.
 *
 * @param <T> the type of object for the content type
 * @param key        content type key
 * @param lineComparator the line comparator for this content type; may be null if the lines are not ordered
 * @param charset    the character set for this content type, may be null
 */
class ContentType<T>
@JvmOverloads constructor(
    /**
     * Content type key
     */
    val key: ContentTypeKey,

    /**
     * Comparator that can be used to determine ordering between different lines of data in the resource.
     * It imposes an ordering on the lines in the data file
     * This is used for searching
     * If the data in the resource is not ordered, then this property is null.
     */
    val lineComparator: ILineComparator?,

    /**
     * Character set used by the data
     */
    override val charset: Charset? = null,

) : IHasPOS, IHasCharset {

    val dataType: DataType<T>
        get() {
            return key.getDataType<T>()
        }

    override val pOS: POS?
        get() {
            return key.pOS
        }

    override fun toString(): String {
        return if (key.pOS != null) {
            "[ContentType: " + key.getDataType<Any?>().toString() + "/" + key.pOS + "]"
        } else {
            "[ContentType: " + key.getDataType<Any?>().toString() + "]"
        }
    }

    companion object {

        val INDEX_NOUN = ContentType<IndexWord>(ContentTypeKey.INDEX_NOUN, IndexLineComparator)
        val INDEX_VERB = ContentType<IndexWord>(ContentTypeKey.INDEX_VERB, IndexLineComparator)
        val INDEX_ADVERB = ContentType<IndexWord>(ContentTypeKey.INDEX_ADVERB, IndexLineComparator)
        val INDEX_ADJECTIVE = ContentType<IndexWord>(ContentTypeKey.INDEX_ADJECTIVE, IndexLineComparator)

        val WORD_NOUN = ContentType<IndexWord>(ContentTypeKey.WORD_NOUN, IndexLineComparator)
        val WORD_VERB = ContentType<IndexWord>(ContentTypeKey.WORD_VERB, IndexLineComparator)
        val WORD_ADVERB = ContentType<IndexWord>(ContentTypeKey.WORD_ADVERB, IndexLineComparator)
        val WORD_ADJECTIVE = ContentType<IndexWord>(ContentTypeKey.WORD_ADJECTIVE, IndexLineComparator)

        val DATA_NOUN = ContentType<Synset>(ContentTypeKey.DATA_NOUN, DataLineComparator)
        val DATA_VERB = ContentType<Synset>(ContentTypeKey.DATA_VERB, DataLineComparator)
        val DATA_ADVERB = ContentType<Synset>(ContentTypeKey.DATA_ADVERB, DataLineComparator)
        val DATA_ADJECTIVE = ContentType<Synset>(ContentTypeKey.DATA_ADJECTIVE, DataLineComparator)

        val EXCEPTION_NOUN = ContentType<ExceptionEntryProxy>(ContentTypeKey.EXCEPTION_NOUN, ExceptionLineComparator)
        val EXCEPTION_VERB = ContentType<ExceptionEntryProxy>(ContentTypeKey.EXCEPTION_VERB, ExceptionLineComparator)
        val EXCEPTION_ADVERB = ContentType<ExceptionEntryProxy>(ContentTypeKey.EXCEPTION_ADVERB, ExceptionLineComparator)
        val EXCEPTION_ADJECTIVE = ContentType<ExceptionEntryProxy>(ContentTypeKey.EXCEPTION_ADJECTIVE, ExceptionLineComparator)

        val SENSE = ContentType<SenseEntry>(ContentTypeKey.SENSE, SenseKeyLineComparator)
        val SENSES = ContentType<Array<SenseEntry>>(ContentTypeKey.SENSES, SenseKeyLineComparator)

        // set of all content types implemented in this class
        private val contentTypes: Set<ContentType<*>> = Collections.unmodifiableSet<ContentType<*>>(
            setOf(
                INDEX_NOUN,
                INDEX_VERB,
                INDEX_ADVERB,
                INDEX_ADJECTIVE,
                WORD_NOUN,
                WORD_VERB,
                WORD_ADVERB,
                WORD_ADJECTIVE,
                DATA_NOUN,
                DATA_VERB,
                DATA_ADVERB,
                DATA_ADJECTIVE,
                EXCEPTION_NOUN,
                EXCEPTION_VERB,
                EXCEPTION_ADVERB,
                EXCEPTION_ADJECTIVE,
                SENSE,
                SENSES,
            )
        )

        /**
         * Emulates the Enum.values() function.
         *
         * @return all the static ContentType instances listed in the class, in the
         * order they are declared.
         * @since JWI 2.0.0
         */
        @JvmStatic
        fun values(): Collection<ContentType<*>> {
            return contentTypes
        }

        /**
         * Use this convenience method to retrieve the appropriate
         * `IndexWord` content type for the specified POS.
         *
         * @param pos the part of speech for the content type, may not be null
         * @return the index content type for the specified part of speech
         * @throws NullPointerException if the specified part of speech is null
         * @since JWI 2.0.0
         */
        fun getIndexContentType(pos: POS): ContentType<IndexWord> {
            return when (pos) {
                POS.NOUN      -> INDEX_NOUN
                POS.VERB      -> INDEX_VERB
                POS.ADVERB    -> INDEX_ADVERB
                POS.ADJECTIVE -> INDEX_ADJECTIVE
            }
            throw IllegalStateException("This should not happen.")
        }

        fun getWordContentType(pos: POS): ContentType<IndexWord> {
            return when (pos) {
                POS.NOUN      -> WORD_NOUN
                POS.VERB      -> WORD_VERB
                POS.ADVERB    -> WORD_ADVERB
                POS.ADJECTIVE -> WORD_ADJECTIVE
            }
            throw IllegalStateException("This should not happen.")
        }

        /**
         * Use this convenience method to retrieve the appropriate
         * `Synset` content type for the specified POS.
         *
         * @param pos the part of speech for the content type, may not be
         * null
         * @return the index content type for the specified part of speech
         * @throws NullPointerException if the specified part of speech is null
         * @since JWI 2.0.0
         */
        fun getDataContentType(pos: POS): ContentType<Synset> {
            return when (pos) {
                POS.NOUN      -> DATA_NOUN
                POS.VERB      -> DATA_VERB
                POS.ADVERB    -> DATA_ADVERB
                POS.ADJECTIVE -> DATA_ADJECTIVE
            }
            throw IllegalStateException("How in the world did we get here?")
        }

        /**
         * Use this convenience method to retrieve the appropriate
         * `ExceptionEntryProxy` content type for the specified POS.
         *
         * @param pos the part of speech for the content type, may not be
         * null
         * @return the index content type for the specified part of speech
         * @throws NullPointerException if the specified part of speech is null
         * @since JWI 2.0.0
         */
        fun getExceptionContentType(pos: POS): ContentType<ExceptionEntryProxy> {
            return when (pos) {
                POS.NOUN      -> EXCEPTION_NOUN
                POS.VERB      -> EXCEPTION_VERB
                POS.ADVERB    -> EXCEPTION_ADVERB
                POS.ADJECTIVE -> EXCEPTION_ADJECTIVE
            }
            throw IllegalStateException("Great Scott, there's been a rupture in the space-time continuum!")
        }
    }
}
