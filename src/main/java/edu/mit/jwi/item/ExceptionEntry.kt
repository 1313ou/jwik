package edu.mit.jwi.item

/**
 * Exception Entry
 */
class ExceptionEntry : ExceptionEntryProxy, IHasPOS, IItem<ExceptionEntryID> {

    override val pOS: POS

    override val iD: ExceptionEntryID

    /**
     * Creates a new exception entry for the specified part-of-speech using the information in the specified exception proxy object.
     *
     * @param proxy the proxy containing the information for the entry
     * @param pos the part-of-speech for the entry
     */
    constructor(proxy: ExceptionEntryProxy, pos: POS) : super(proxy) {
        this.pOS = pos
        this.iD = ExceptionEntryID(surfaceForm, pos)
    }

    /**
     * Creates a new exception entry for the specified part-of-speech using the specified surface and root forms.
     *
     * @param surfaceForm the surface form for the entry
     * @param pos the part-of-speech for the entry
     * @param rootForms the root forms for the entry
     */
    constructor(surfaceForm: String, pos: POS, rootForms: Collection<String>) : super(surfaceForm, rootForms) {
        this.iD = ExceptionEntryID(surfaceForm, pos)
        this.pOS = pos
    }

    override fun toString(): String {
        return "${super.toString()}-$pOS"
    }
}
