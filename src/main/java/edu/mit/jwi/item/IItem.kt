package edu.mit.jwi.item

import java.io.Serializable

/**
 * An item is an object with an ID.
 */
interface IItem<D> : Serializable where D : IItemID {

    val iD: D
}
