package edu.mit.jwi.test

import edu.mit.jwi.data.parse.SenseKeyParser
import edu.mit.jwi.item.LexFile.Companion.NOUN_LOCATION
import edu.mit.jwi.item.LexFile.Companion.NOUN_TOPS
import edu.mit.jwi.item.POS
import edu.mit.jwi.item.SenseKey
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SensekeyTests {

    @Test
    fun sensekeyWithSameOrDifferentLemma() {
        val sk1 = SenseKey("entity", POS.NOUN, 0, NOUN_TOPS.number, false)
        val sk2 = SenseKey("entity", POS.NOUN, 0, NOUN_TOPS.number, false)
        val sk3 = SenseKey("'entity", POS.NOUN, 0, NOUN_TOPS.number, false)
        println(sk1)
        println(sk2)
        println(sk3)
        Assertions.assertEquals(sk1, sk2)
        Assertions.assertEquals(sk1.hashCode(), sk2.hashCode())
        Assertions.assertNotEquals(sk1, sk3)
    }

    @Test
    fun sensekeyDifferingOnCase() {
        val sk1 = SenseKey("'s_Gravenhage", POS.NOUN, 0, NOUN_LOCATION.number, false)
        val sk2 = SenseKey("'s_gravenhage", POS.NOUN, 0, NOUN_LOCATION.number, false)
        println(sk1)
        println(sk2)
        Assertions.assertEquals(sk1, sk2)
        Assertions.assertEquals(sk1.hashCode(), sk2.hashCode())
    }

    @Test
    fun sensekeyDifferingOnCaseFromLemma() {
        val sk1 = SenseKey("Earth", POS.NOUN, 0, NOUN_LOCATION.number, false)
        val sk2 = SenseKey("earth", POS.NOUN, 0, NOUN_LOCATION.number, false)
        println(sk1)
        println(sk2)
        Assertions.assertEquals(sk1, sk2)
        Assertions.assertEquals(sk1.hashCode(), sk2.hashCode())
    }

    @Test
    fun sensekeyEquals() {
        val sk1 = SenseKeyParser.parseLine("earth%1:15:00::")
        val sk2 = SenseKeyParser.parseLine("earth%1:15:00::")
        println(sk1)
        println(sk2)
        Assertions.assertEquals(sk1, sk2)
        Assertions.assertEquals(sk1.hashCode(), sk2.hashCode())
    }
}