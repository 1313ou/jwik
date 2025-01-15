package edu.mit.jwi.item

import java.util.regex.Pattern

// sense keys

fun String.asSensekeyLemma(): String{
    return this.lowercase()
}

// index words

private val whitespace: Pattern = Pattern.compile("\\s+")

fun String.asIndexWordLemma(): String{
    return this.lowercase().trim { it <= ' ' }
}

fun String.asEscapedIndexWordLemma(): String{
    return  whitespace.matcher(this.asIndexWordLemma()).replaceAll("_")
}

// exceptions

fun String.asSurfaceForm(): String{
    return this.lowercase().trim { it <= ' ' }
}
