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

/**
 * The three different possible syntactic markers indicating limitations on the
 * syntactic position an adjective may have in relation to the noun it modifies.
 *
 * @property symbol      the symbol, may not be empty
 * @property description the description, may not be empty
 *
 * @author Mark A. Finlayson
 * @version 2.4.0
 * @since JWI 2.1.0
 */
enum class AdjMarker(
    /**
     * The adjective marker symbol, as found appended to the ends of adjective words in the data files, parenthesis included.
     * @since JWI 2.1.0
     */
    val symbol: String,
    /**
     * A user-readable description of the type of marker, drawn from the Wordnet specification.
     * @since JWI 2.1.0
     */
    val description: String,
) {

    PREDICATE("(p)", "predicate position"),
    PRENOMINAL("(a)", "prenominal (attributive) position"),
    POSTNOMINAL("(ip)", "immediately postnominal position");

    /**
     * Constructs a new adjective marker with the specified symbol and description
     *
     * @throws IllegalArgumentException if either argument is empty or all whitespace
     * @since JWI 2.1.0
     */
    init {
        require(symbol.isNotEmpty())
        require(description.isNotEmpty())
    }
}
