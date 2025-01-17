package edu.mit.jwi.item

import java.util.regex.Pattern

// sense keys

fun String.asSensekeyLemma(): String {
    return this.lowercase()
}

// indexes

private val whitespace: Pattern = Pattern.compile("\\s+")

fun String.asIndexLemma(): String {
    return this.lowercase().trim { it <= ' ' }
}

fun String.asEscapedSenseIndexLemma(): String {
    return whitespace.matcher(this.asIndexLemma()).replaceAll("_")
}

// exceptions

fun String.asSurfaceForm(): String {
    return this.lowercase().trim { it <= ' ' }
}
