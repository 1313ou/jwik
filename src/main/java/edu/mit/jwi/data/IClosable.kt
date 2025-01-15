package edu.mit.jwi.data

/**
 * An object that can be closed. What 'closing' means is implementation specific.
 */
interface IClosable {

    /**
     * This closes the object by disposing of data backing objects or connections.
     * If the object is already closed, or in the process of closing, this method does nothing (although, if the object is in the process of closing, it may block until closing is complete).
     */
    fun close()
}
