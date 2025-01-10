package edu.mit.jwi.test

import edu.mit.jwi.item.POS
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream

class JWIWordStartTests {

    @Test
    fun searchStart() {
        val result: Set<String> = jwi!!.dict.getWords(start!!, pos, 0)
        PS.println(start)
        PS.println(result)
    }

    @Test
    fun searchStartLimited() {
        val result: Set<String> = jwi!!.dict.getWords(start!!, pos, 3)
        PS.println(start)
        PS.println(result)
    }

    companion object {

        private val VERBOSE = !System.getProperties().containsKey("SILENT")

        private val PS: PrintStream = if (VERBOSE) System.out else PrintStream(object : OutputStream() {
            override fun write(b: Int) {
                //DO NOTHING
            }
        })

        private var jwi: JWI? = null
        private var start: String? = null
        private var pos: POS? = null

        @JvmStatic
        @BeforeAll
        @Throws(IOException::class)
        fun init() {
            val wnHome = System.getProperty("SOURCE")
            jwi = JWI(wnHome)

            start = System.getProperty("TARGET")

            val scope = System.getProperty("TARGETSCOPE")
            pos = try {
                POS.valueOf(scope)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }
}