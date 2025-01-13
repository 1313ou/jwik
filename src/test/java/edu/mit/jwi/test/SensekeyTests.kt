package edu.mit.jwi.test

import edu.mit.jwi.item.LexFile.Companion.NOUN_LOCATION
import edu.mit.jwi.item.LexFile.Companion.NOUN_TOPS
import edu.mit.jwi.item.POS
import edu.mit.jwi.item.SenseKey
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.OutputStream
import java.io.PrintStream

class SensekeyTests {

    @Test
    fun sensekey() {
        val sk1 = SenseKey("entity", 0, POS.NOUN, false, NOUN_TOPS)
        val sk2 = SenseKey("entity", 0, POS.NOUN, false, NOUN_TOPS)
        val sk3 = SenseKey("'entity", 0, POS.NOUN, false, NOUN_TOPS)
        println(sk1)
        println(sk2)
        println(sk3)
        Assertions.assertEquals(sk1, sk2)
        Assertions.assertEquals(sk1.hashCode(), sk2.hashCode())
        Assertions.assertNotEquals(sk1, sk3)
    }

    @Test
    fun sensekey2() {
        val sk1 = SenseKey("'s_Gravenhage", 0, POS.NOUN, false, NOUN_LOCATION)
        val sk2 = SenseKey("'s_gravenhage", 0, POS.NOUN, false, NOUN_LOCATION)
        println(sk1)
        println(sk2)
        Assertions.assertEquals(sk1, sk2)
        Assertions.assertEquals(sk1.hashCode(), sk2.hashCode())
    }

    companion object {

        private val VERBOSE = !System.getProperties().containsKey("SILENT")

        private val PS: PrintStream? = if (VERBOSE) System.out else PrintStream(object : OutputStream() {
            override fun write(b: Int) {
                //DO NOTHING
            }
        })
    }
}