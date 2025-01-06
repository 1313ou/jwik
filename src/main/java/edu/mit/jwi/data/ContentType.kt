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
import java.lang.reflect.Field
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
 * @param lineComparator the line comparator for this content type; may be `null` if the lines are not ordered
 * @param charset    the character set for this content type, may be `null`
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.0.0
</T> */
class ContentType<T> @JvmOverloads constructor(
    override val key: ContentTypeKey,
    override val lineComparator: ILineComparator?,
    override val charset: Charset? = null
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

        val INDEX_NOUN: ContentType<IIndexWord> = ContentType<IIndexWord>(ContentTypeKey.INDEX_NOUN, IndexLineComparator.getInstance())
        val INDEX_VERB: ContentType<IIndexWord> = ContentType<IIndexWord>(ContentTypeKey.INDEX_VERB, IndexLineComparator.getInstance())
        val INDEX_ADVERB: ContentType<IIndexWord> = ContentType<IIndexWord>(ContentTypeKey.INDEX_ADVERB, IndexLineComparator.getInstance())
        val INDEX_ADJECTIVE: ContentType<IIndexWord> = ContentType<IIndexWord>(ContentTypeKey.INDEX_ADJECTIVE, IndexLineComparator.getInstance())

        val WORD_NOUN: ContentType<IIndexWord> = ContentType<IIndexWord>(ContentTypeKey.WORD_NOUN, IndexLineComparator.getInstance())
        val WORD_VERB: ContentType<IIndexWord> = ContentType<IIndexWord>(ContentTypeKey.WORD_VERB, IndexLineComparator.getInstance())
        val WORD_ADVERB: ContentType<IIndexWord> = ContentType<IIndexWord>(ContentTypeKey.WORD_ADVERB, IndexLineComparator.getInstance())
        val WORD_ADJECTIVE: ContentType<IIndexWord> = ContentType<IIndexWord>(ContentTypeKey.WORD_ADJECTIVE, IndexLineComparator.getInstance())

        val DATA_NOUN: ContentType<ISynset> = ContentType<ISynset>(ContentTypeKey.DATA_NOUN, DataLineComparator.getInstance())
        val DATA_VERB: ContentType<ISynset> = ContentType<ISynset>(ContentTypeKey.DATA_VERB, DataLineComparator.getInstance())
        val DATA_ADVERB: ContentType<ISynset> = ContentType<ISynset>(ContentTypeKey.DATA_ADVERB, DataLineComparator.getInstance())
        val DATA_ADJECTIVE: ContentType<ISynset> = ContentType<ISynset>(ContentTypeKey.DATA_ADJECTIVE, DataLineComparator.getInstance())

        val EXCEPTION_NOUN: ContentType<IExceptionEntryProxy> = ContentType<IExceptionEntryProxy>(
            ContentTypeKey.EXCEPTION_NOUN,
            ExceptionLineComparator.getInstance()
        )
        val EXCEPTION_VERB: ContentType<IExceptionEntryProxy> = ContentType<IExceptionEntryProxy>(
            ContentTypeKey.EXCEPTION_VERB,
            ExceptionLineComparator.getInstance()
        )
        val EXCEPTION_ADVERB: ContentType<IExceptionEntryProxy> = ContentType<IExceptionEntryProxy>(
            ContentTypeKey.EXCEPTION_ADVERB,
            ExceptionLineComparator.getInstance()
        )
        val EXCEPTION_ADJECTIVE: ContentType<IExceptionEntryProxy> = ContentType<IExceptionEntryProxy>(
            ContentTypeKey.EXCEPTION_ADJECTIVE,
            ExceptionLineComparator.getInstance()
        )

        val SENSE: ContentType<ISenseEntry> = ContentType<ISenseEntry>(ContentTypeKey.SENSE, SenseKeyLineComparator.getInstance())
        val SENSES: ContentType<Array<ISenseEntry>> = ContentType<Array<ISenseEntry>>(ContentTypeKey.SENSES, SenseKeyLineComparator.getInstance())

        // set of all content types implemented in this class
        private val contentTypes: Set<ContentType<*>>

        // initialization for static content type set
        init {
            // get all the fields containing ContentType
            val fields = ContentType::class.java.getFields()
            val instanceFields: MutableList<Field> = ArrayList<Field>()
            for (field in fields) {
                if (field.type == ContentType::class.java) {
                    instanceFields.add(field)
                }
            }

            // this is the backing set
            val hidden: MutableSet<ContentType<*>?> = LinkedHashSet<ContentType<*>?>(instanceFields.size)

            // fill in the backing set
            var contentType: ContentType<*>?
            for (field in instanceFields) {
                try {
                    contentType = field.get(null) as ContentType<*>?
                    if (contentType == null) {
                        continue
                    }
                    hidden.add(contentType)
                } catch (_: IllegalAccessException) {
                    // ignore
                }
            }

            // make the value set unmodifiable
            contentTypes = Collections.unmodifiableSet<ContentType<*>>(hidden)
        }

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
         * `IIndexWord` content type for the specified POS.
         *
         * @param pos the part of speech for the content type, may not be `null`
         * @return the index content type for the specified part of speech
         * @throws NullPointerException if the specified part of speech is `null`
         * @since JWI 2.0.0
         */
        fun getIndexContentType(pos: POS): IContentType<IIndexWord> {
             return when (pos) {
                POS.NOUN      -> INDEX_NOUN
                POS.VERB      -> INDEX_VERB
                POS.ADVERB    -> INDEX_ADVERB
                POS.ADJECTIVE -> INDEX_ADJECTIVE
            }
            throw IllegalStateException("This should not happen.")
        }

        fun getWordContentType(pos: POS): IContentType<IIndexWord> {
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
         * `ISynset` content type for the specified POS.
         *
         * @param pos the part of speech for the content type, may not be
         * `null`
         * @return the index content type for the specified part of speech
         * @throws NullPointerException if the specified part of speech is `null`
         * @since JWI 2.0.0
         */
        fun getDataContentType(pos: POS): IContentType<ISynset> {
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
         * `null`
         * @return the index content type for the specified part of speech
         * @throws NullPointerException if the specified part of speech is `null`
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
