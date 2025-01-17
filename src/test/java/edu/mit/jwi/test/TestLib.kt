package edu.mit.jwi.test

import edu.mit.jwi.data.parse.DataLineParser
import edu.mit.jwi.data.parse.SenseKeyParser
import edu.mit.jwi.item.*
import org.junit.jupiter.api.Assertions
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

object TestLib {

    @JvmStatic
    fun sensekeyFromStringIsLive(jwi: JWI, skStr: String): Boolean {
        val sk = SenseKeyParser.parseLine(skStr)
        Assertions.assertEquals(sk.toString(), skStr)
        return sensekeyIsLive(jwi, sk)
    }

    fun sensekeyIsLive(jwi: JWI, sk: SenseKey): Boolean {
        // println("● sensekey=" + sk)
        val senseEntry = jwi.dict.getSenseEntry(sk)
        if (senseEntry == null) {
            return false
        }
        val offset = senseEntry.offset
        val sid = SynsetID(offset, sk.pOS)
        return jwi.dict.getSynset(sid) != null
    }

    @JvmStatic
    fun listDeadSensekeys(jwi: JWI) {
        val errorCount = AtomicInteger()
        jwi.forAllSenses(Consumer { sense: Sense? ->
            val sk = sense!!.senseKey
            val isLive = sensekeyIsLive(jwi, sk)
            if (!isLive) {
                System.err.println("☈ sense = $sense generated sensekey=$sk not found")
                //throw new IllegalArgumentException(sk.toString())
                errorCount.getAndIncrement()
            }
        })
        Assertions.assertEquals(0, errorCount.get())
    }

    @JvmStatic
    fun allSensekeysAreLive(jwi: JWI) {
        jwi.forAllSensekeys(Consumer { sk: SenseKey? ->
            Assertions.assertNotNull(sk)
            val senseEntry = jwi.dict.getSenseEntry(sk!!)
            Assertions.assertNotNull(senseEntry)
            val offset = senseEntry!!.offset
            val sid = SynsetID(offset, sk.pOS)
            Assertions.assertNotNull(sid)
            val synset = jwi.dict.getSynset(sid)
            Assertions.assertNotNull(synset)
        })
    }

    @JvmStatic
    fun allSenseEntriesAreLive(jwi: JWI) {
        jwi.forAllSenseEntries(Consumer { se: SenseEntry? ->
            Assertions.assertNotNull(se)
            val offset = se!!.offset
            val pos = se.pOS
            val sid = SynsetID(offset, pos)
            Assertions.assertNotNull(sid)
            val synset = jwi.dict.getSynset(sid)
            Assertions.assertNotNull(synset)
        })
    }

    @JvmStatic

    fun parseDataLineIntoMembers(line: String): MutableList<String?> {
        val result: MutableList<String?> = ArrayList<String?>()
        val synset = DataLineParser.parseLine(line)
        for (sense in synset.senses) {
            result.add("$sense, ${sense.lemma}, ${sense.lexicalID}")
        }
        return result
    }
}
