package edu.mit.jwi.test

import edu.mit.jwi.test.TestLib.parseDataLineIntoMembers
import org.junit.jupiter.api.Test
import java.io.OutputStream
import java.io.PrintStream
import java.util.function.Consumer

class LexidTests {

    @Test
    fun parseCompatLexid() {
        val line = "02504828 00 s 01 hot 03 001 & 02504619 a 0000 | (color) bold and intense; \"hot pink\""
        parseDataLineIntoMembers(line).forEach(Consumer { x: String? -> PS.println(x) })
    }

    @Test
    fun parseNonCompatLexid() {
        val line = "02504828 00 s 01 hot 13 001 & 02504619 a 0000 | (color) bold and intense; \"hot pink\""
        parseDataLineIntoMembers(line).forEach(Consumer { x: String? -> PS.println(x) })
    }

    companion object {

        private val VERBOSE = !System.getProperties().containsKey("SILENT")

        private val PS: PrintStream = if (VERBOSE) System.out else PrintStream(object : OutputStream() {
            override fun write(b: Int) {
                //DO NOTHING
            }
        })
    }
}