package edu.mit.jwi.data.compare

/**
 * A string comparator.
 * The compare method of this class will throw an IllegalArgumentException if the line data passed to that method is ill-formed.
 */
interface ILineComparator : Comparator<String> {

    var commentDetector: ICommentDetector?
}
