package edu.mit.jwi.test

import org.junit.jupiter.api.Test

open class Word(val lemma: String) {

    override fun toString(): String {
        return lemma
    }
}

class Synset2(val id: String, val words: List<Word>) {

    inner class Sense2(val lemma: Word) {

        val synset: Synset2
            get() = this@Synset2

        override fun toString(): String {
            return "Sense2(lemma=$lemma, synset=$synset)"
        }
    }

    override fun toString(): String {
        return "Synset2(id='$id')"
    }

    val senses: List<Sense2> by lazy { words.map { Sense2(it) } }

    fun sense(i: Int): Sense2 = senses[i]
}

class WIPTests {

    @Test
    fun wip() {
        val s = listOf(
            Word("try"),
            Word("it"),
            Word("hard"),
        )
        val y = Synset2("mysynset", s)
        val ss = y.senses
        println(y.sense(1))
        ss.forEach { println(it) }
    }
}