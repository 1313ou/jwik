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
        val sk = checkNotNull(SenseKeyParser.instance!!.parseLine(skStr))
        Assertions.assertEquals(sk.toString(), skStr)
        return sensekeyIsLive(jwi, sk)
    }

    fun sensekeyIsLive(jwi: JWI, sk: ISenseKey): Boolean {
        // println("● sensekey=" + sk)
        val senseEntry = jwi.dict.getSenseEntry(sk)
        if (senseEntry == null) {
            return false
        }
        val offset = senseEntry.offset
        val sid = SynsetID(offset, sk.pOS!!)
        return jwi.dict.getSynset(sid) != null
    }

    @JvmStatic
    fun listDeadSensekeys(jwi: JWI) {
        val errorCount = AtomicInteger()
        jwi.forAllSenses(Consumer { sense: Word? ->
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
        jwi.forAllSensekeys(Consumer { sk: ISenseKey? ->
            Assertions.assertNotNull(sk)
            val senseEntry = jwi.dict.getSenseEntry(sk!!)
            Assertions.assertNotNull(senseEntry)
            val offset = senseEntry!!.offset
            val sid = SynsetID(offset, sk.pOS!!)
            Assertions.assertNotNull(sid)
            val synset = jwi.dict.getSynset(sid)
            Assertions.assertNotNull(synset)
        })
    }

    @JvmStatic
    fun allSenseEntriesAreLive(jwi: JWI) {
        jwi.forAllSenseEntries(Consumer { se: ISenseEntry? ->
            Assertions.assertNotNull(se)
            val offset = se!!.offset
            val pos = se.pOS
            val sid: ISynsetID = SynsetID(offset, pos!!)
            Assertions.assertNotNull(sid)
            val synset = jwi.dict.getSynset(sid)
            Assertions.assertNotNull(synset)
        })
    }

    @JvmStatic

    fun parseDataLineIntoMembers(line: String): MutableList<String?> {
        val result: MutableList<String?> = ArrayList<String?>()
        val parser = DataLineParser.instance
        val synset = parser!!.parseLine(line)
        for (sense in synset.words) {
            result.add(String.format("%s %s %d", sense, sense.lemma, sense.lexicalID))
        }
        return result
    }
}
