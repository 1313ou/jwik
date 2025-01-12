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
 * A concrete implementation of the `IContentType` interface. This class
 * provides the content types necessary for Wordnet in the form of static
 * fields. It is not implemented as an `Enum` so that clients may add
 * their own content types by instantiating this class.
 *
 * @param <T> the type of object for the content type
 * @param key        content type key
 * @param lineComparator the line comparator for this content type; may be null if the lines are not ordered
 * @param charset    the character set for this content type, may be null
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.0.0
 */
class ContentType<T>
@JvmOverloads constructor(
    override val key: ContentTypeKey,
    override val lineComparator: ILineComparator?,
    override val charset: Charset? = null,
) : IContentType<T> {

    override val dataType: IDataType<T>
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

        val INDEX_NOUN: ContentType<IndexWord> = ContentType<IndexWord>(ContentTypeKey.INDEX_NOUN, IndexLineComparator.instance)
        val INDEX_VERB: ContentType<IndexWord> = ContentType<IndexWord>(ContentTypeKey.INDEX_VERB, IndexLineComparator.instance)
        val INDEX_ADVERB: ContentType<IndexWord> = ContentType<IndexWord>(ContentTypeKey.INDEX_ADVERB, IndexLineComparator.instance)
        val INDEX_ADJECTIVE: ContentType<IndexWord> = ContentType<IndexWord>(ContentTypeKey.INDEX_ADJECTIVE, IndexLineComparator.instance)

        val WORD_NOUN: ContentType<IndexWord> = ContentType<IndexWord>(ContentTypeKey.WORD_NOUN, IndexLineComparator.instance)
        val WORD_VERB: ContentType<IndexWord> = ContentType<IndexWord>(ContentTypeKey.WORD_VERB, IndexLineComparator.instance)
        val WORD_ADVERB: ContentType<IndexWord> = ContentType<IndexWord>(ContentTypeKey.WORD_ADVERB, IndexLineComparator.instance)
        val WORD_ADJECTIVE: ContentType<IndexWord> = ContentType<IndexWord>(ContentTypeKey.WORD_ADJECTIVE, IndexLineComparator.instance)

        val DATA_NOUN: ContentType<Synset> = ContentType<Synset>(ContentTypeKey.DATA_NOUN, DataLineComparator.instance)
        val DATA_VERB: ContentType<Synset> = ContentType<Synset>(ContentTypeKey.DATA_VERB, DataLineComparator.instance)
        val DATA_ADVERB: ContentType<Synset> = ContentType<Synset>(ContentTypeKey.DATA_ADVERB, DataLineComparator.instance)
        val DATA_ADJECTIVE: ContentType<Synset> = ContentType<Synset>(ContentTypeKey.DATA_ADJECTIVE, DataLineComparator.instance)

        val EXCEPTION_NOUN: ContentType<IExceptionEntryProxy> = ContentType<IExceptionEntryProxy>(ContentTypeKey.EXCEPTION_NOUN, ExceptionLineComparator.instance)
        val EXCEPTION_VERB: ContentType<IExceptionEntryProxy> = ContentType<IExceptionEntryProxy>(ContentTypeKey.EXCEPTION_VERB, ExceptionLineComparator.instance)
        val EXCEPTION_ADVERB: ContentType<IExceptionEntryProxy> = ContentType<IExceptionEntryProxy>(ContentTypeKey.EXCEPTION_ADVERB, ExceptionLineComparator.instance)
        val EXCEPTION_ADJECTIVE: ContentType<IExceptionEntryProxy> = ContentType<IExceptionEntryProxy>(ContentTypeKey.EXCEPTION_ADJECTIVE, ExceptionLineComparator.instance)

        val SENSE: ContentType<SenseEntry> = ContentType<SenseEntry>(ContentTypeKey.SENSE, SenseKeyLineComparator.instance)
        val SENSES: ContentType<Array<SenseEntry>> = ContentType<Array<SenseEntry>>(ContentTypeKey.SENSES, SenseKeyLineComparator.instance)

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
        fun getIndexContentType(pos: POS): IContentType<IndexWord> {
            return when (pos) {
                POS.NOUN      -> INDEX_NOUN
                POS.VERB      -> INDEX_VERB
                POS.ADVERB    -> INDEX_ADVERB
                POS.ADJECTIVE -> INDEX_ADJECTIVE
            }
            throw IllegalStateException("This should not happen.")
        }

        fun getWordContentType(pos: POS): IContentType<IndexWord> {
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
        fun getDataContentType(pos: POS): IContentType<Synset> {
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
         * `IExceptionEntryProxy` content type for the specified POS.
         *
         * @param pos the part of speech for the content type, may not be
         * null
         * @return the index content type for the specified part of speech
         * @throws NullPointerException if the specified part of speech is null
         * @since JWI 2.0.0
         */
        fun getExceptionContentType(pos: POS): IContentType<IExceptionEntryProxy> {
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
