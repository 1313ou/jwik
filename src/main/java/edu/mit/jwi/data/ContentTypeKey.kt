package edu.mit.jwi.data

import edu.mit.jwi.item.POS

/**
 * Content type keys.
 */
enum class ContentTypeKey(
    private val dataType: DataType<*>,
    val pOS: POS?
) {

    INDEX_NOUN(DataType.INDEX, POS.NOUN),  //
    INDEX_VERB(DataType.INDEX, POS.VERB),  //
    INDEX_ADVERB(DataType.INDEX, POS.ADVERB),  //
    INDEX_ADJECTIVE(DataType.INDEX, POS.ADJECTIVE),  //
    WORD_NOUN(DataType.WORD, POS.NOUN),  //
    WORD_VERB(DataType.WORD, POS.VERB),  //
    WORD_ADVERB(DataType.WORD, POS.ADVERB),  //
    WORD_ADJECTIVE(DataType.WORD, POS.ADJECTIVE),  //
    DATA_NOUN(DataType.DATA, POS.NOUN),  //
    DATA_VERB(DataType.DATA, POS.VERB),  //
    DATA_ADVERB(DataType.DATA, POS.ADVERB),  //
    DATA_ADJECTIVE(DataType.DATA, POS.ADJECTIVE),  //
    EXCEPTION_NOUN(DataType.EXCEPTION, POS.NOUN),  //
    EXCEPTION_VERB(DataType.EXCEPTION, POS.VERB),  //
    EXCEPTION_ADVERB(DataType.EXCEPTION, POS.ADVERB),  //
    EXCEPTION_ADJECTIVE(DataType.EXCEPTION, POS.ADJECTIVE),  //
    SENSE(DataType.SENSE, null), SENSES(DataType.SENSES, null);

    fun <T> getDataType(): DataType<T> {
        @Suppress("UNCHECKED_CAST")
        return dataType as DataType<T>
    }
}