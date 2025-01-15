package edu.mit.jwi.item

/**
 * A unique identifier sufficient to retrieve a particular word from the Wordnet
 * database. Consists of a synset id, sense number, and lemma.
 */
interface IWordID : IHasPOS, IItemID {

    val synsetID: SynsetID
}
