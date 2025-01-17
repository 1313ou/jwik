package edu.mit.jwi.test

import edu.mit.jwi.item.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream
import java.util.function.Consumer

class JWITests {

    // enum
    @Test
    fun allSenses() {
        jwi!!.forAllSenses(null)
    }

    @Test
    fun allSynsets() {
        jwi!!.forAllSynsets(null)
    }

    @Test
    fun allSenseEntries() {
        jwi!!.forAllSenseEntries(null)
    }

    // enum non null
    @Test
    fun allSensesAreNonNull() {
        jwi!!.forAllSenses { }
    }

    @Test
    fun allSynsetsAreNonNull() {
        jwi!!.forAllSynsets { }
    }

    @Test
    fun allSenseEntriesAreNonNull() {
        jwi!!.forAllSenseEntries { }
    }

    @Test
    fun allLemmasAreNonNull() {
        jwi!!.forAllLemmas { l: String? ->
            Assertions.assertNotNull(l)
            Assertions.assertFalse(l!!.isEmpty())
        }
    }

    @Test
    fun allSensekeysAreNonNull() {
        jwi!!.forAllSensekeys { }
    }

    @Test
    fun allSynsetRelationsAreNonNull() {
        jwi!!.forAllSynsetRelations { }
    }

    @Test
    fun allSenseRelationsAreNonNull() {
        jwi!!.forAllSenseRelations { }
    }

    // enum live
    @Test
    fun allSensekeysAreLive() {
        TestLib.allSensekeysAreLive(jwi!!)
    }

    @Test
    fun allSenseEntriesAreLive() {
        TestLib.allSenseEntriesAreLive(jwi!!)
    }

    // others
    // the test involves new is_caused_by
    @Test
    fun extraRelations() {
        jwi!!.walk("spread", PS)
    }

    // the test involves Young (n) and adj
    @Test
    fun cased() {
        jwi!!.walk("young", PS)
    }

    // the test involves new is_caused_by
    @Test
    fun cased2() {
        jwi!!.walk("aborigine", PS)
    }

    // the test involves adj
    @Test
    fun adjSat() {
        jwi!!.walk("small", PS)
    }

    // the test involves galore (a)
    @Test
    fun adjMarker() {
        jwi!!.walk("galore", PS)
    }

    // the test involves a frameless entry
    @Test
    fun frameless() {
        jwi!!.dict.getIndex("fangirl", POS.VERB)
        jwi!!.walk("fangirl", POS.VERB, PS)
    }

    companion object {

        private val VERBOSE = !System.getProperties().containsKey("SILENT")

        private val PS: PrintStream = if (VERBOSE) System.out else PrintStream(object : OutputStream() {
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