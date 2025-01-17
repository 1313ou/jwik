package edu.mit.jwi.test

import edu.mit.jwi.data.parse.DataLineParser
import edu.mit.jwi.data.parse.SenseKeyParser
import edu.mit.jwi.item.LexFile.Companion.NOUN_LOCATION
import edu.mit.jwi.item.LexFile.Companion.NOUN_TOPS
import edu.mit.jwi.item.POS
import edu.mit.jwi.item.SenseKey
import edu.mit.jwi.item.Synset
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ParseTests {

    val line = "00001740 00 a 01 able 0 005 = 06026773 n 0000 = 06505125 n 0000 ! 00002101 a 0101 + 06505125 n 0101 + 06026773 n 0101 | (usually followed by ‘to’) having the necessary means or skill or know-how or authority to do something; \"able to swim\" \"She was able to program her computer.\" \"We were at last able to buy a car.\" \"able to get a grant for the project\""

    @Test
    fun parseAdjDataLine() {
        val synset: Synset = DataLineParser.parseLine(line)
        print(synset)
     }
}