/* ******************************************************************************
 * Java Wordnet Interface Library (JWI) v2.4.0
 * Copyright (c) 2007-2015 Mark A. Finlayson
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 4.0
 * International Public License, which means it may be freely used for all
 * purposes, as long as proper acknowledgment is made.  See the license file
 * included with this distribution for more details.
 *******************************************************************************/
package edu.mit.jwi

import edu.mit.jwi.data.IDataProvider

/**
 * A type of `IDictionary` which uses an instance of an
 * `IDataProvider` to obtain its data.
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.0.0
 */
interface IDataSourceDictionary : IDictionary {

    val dataProvider: IDataProvider?
}
