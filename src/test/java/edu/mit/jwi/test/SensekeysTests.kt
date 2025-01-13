package edu.mit.jwi.test

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream

class SensekeysTests {

    @Test
    fun sensekeysLive() {
        try {
            TestLib.allSensekeysAreLive(jwi!!)
        } catch (ae: AssertionError) {
            TestLib.listDeadSensekeys(jwi!!)
            throw ae
        }
    }

    @Test
    fun senseEntriesLive() {
        TestLib.allSenseEntriesAreLive(jwi!!)
    }

    @Test
    fun sensekey() {
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "galore%5:00:00:abundant:00"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "galore%5:00:00:many:00"))

        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "aborigine%1:18:00::"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "aborigine%1:18:01::"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "Aborigine%1:18:00::"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "Aborigine%1:18:01::"))

        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "hot%3:00:01::"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "hot%3:00:02::"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "hot%5:00:00:active:01"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "hot%5:00:00:charged:00"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "hot%5:00:00:eager:00"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "hot%5:00:00:fast:01"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "hot%5:00:00:fresh:01"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "hot%5:00:00:good:01"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "hot%5:00:00:illegal:00"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "hot%5:00:00:lucky:00"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "hot%5:00:00:near:00"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "hot%5:00:00:new:00"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "hot%5:00:00:popular:00"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "hot%5:00:00:radioactive:00"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "hot%5:00:00:sexy:00"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "hot%5:00:00:skilled:00"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "hot%5:00:00:tasty:00"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "hot%5:00:00:unpleasant:00"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "hot%5:00:00:violent:00"))
        Assertions.assertTrue(TestLib.sensekeyFromStringIsLive(jwi!!, "hot%5:00:00:wanted:00"))
    }

    companion object {

        private val VERBOSE = !System.getProperties().containsKey("SILENT")

        private val PS: PrintStream? = if (VERBOSE) System.out else PrintStream(object : OutputStream() {
            override fun write(b: Int) {
                //DO NOTHING
            }
        })

        private var jwi: JWI? = null

        @JvmStatic
        @BeforeAll
        @Throws(IOException::class)
        fun init() {
            val wnHome = System.getProperty("SOURCE")
            jwi = JWI(wnHome)
        }
    }
}