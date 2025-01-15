package edu.mit.jwi.item

import java.util.regex.Pattern

fun String.asSensekeyLemma(): String{
    return this.lowercase()
}

private val whitespace: Pattern = Pattern.compile("\\s+")

fun String.asIndexWordLemma(): String{
    return  whitespace.matcher(this.lowercase().trim { it <= ' ' }).replaceAll("_")
}
