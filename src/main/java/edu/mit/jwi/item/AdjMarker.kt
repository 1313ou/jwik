package edu.mit.jwi.item

/**
 * The three different possible syntactic markers indicating limitations on the
 * syntactic position an adjective may have in relation to the noun it modifies.
 *
 * @property symbol      the symbol, may not be empty
 * @property description the description, may not be empty
 */
enum class AdjMarker(
    /**
     * The adjective marker symbol, as found appended to the ends of adjective words in the data files, parenthesis included.
     */
    val symbol: String,

    /**
     * A user-readable description of the type of marker, drawn from the Wordnet specification.
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
     */
    init {
        require(symbol.isNotEmpty())
        require(description.isNotEmpty())
    }
}
