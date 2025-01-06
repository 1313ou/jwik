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

import edu.mit.jwi.item.POS

/**
 * Content type keys.
 *
 * @author Bernard Bou
 * @version 2.4.1
 * @since JWI 2.4.1
 */
enum class ContentTypeKey(private val fType: IDataType<*>, val pOS: POS?) {

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

    fun <T> getDataType(): IDataType<T> {
        return fType as IDataType<T>
    }
}