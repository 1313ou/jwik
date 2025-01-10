package edu.mit.jwi.test

import edu.mit.jwi.Config
import edu.mit.jwi.Dictionary
import edu.mit.jwi.IDictionary
import edu.mit.jwi.item.*
import java.io.File
import java.io.IOException
import java.io.PrintStream
import java.util.function.Consumer
import kotlin.Throws

/**
 * JWI
 *
 * @author Bernard Bou
 */
class JWI
@JvmOverloads constructor(wnHome: String, config: Config? = null) {

    @JvmField
    val dict: IDictionary

    /**
     * Constructor
     *
     * @param wnHome wordnet home
     * @param config config
     * final Config config = new Config();
     * config.checkLexicalId = false;
     * config.charSet = StandardCharsets.UTF_8;
     * config.indexSenseKeyComparator = Comparators.CaseSensitiveSenseKeyLineComparator.getInstance();
     * @throws IOException io exception
     */
    init {
        System.out.printf("FROM %s%n", wnHome)
        System.out.printf("CONFIG %s%n", config)

        // construct the URL to the WordNet dictionary directory
        val url = File(wnHome).toURI().toURL()

        // construct the dictionary object and open it
        dict = Dictionary(url, config)

        // open it
        dict.open()
    }

    // M A I N   I T E R A T I O N S

    fun forAllSenses(f: Consumer<IWord>?) {
        for (pos in POS.entries) {
            val it: Iterator<IIndexWord> = dict.getIndexWordIterator(pos)
            while (it.hasNext()) {
                val idx = it.next()
                val senseids: List<IWordID> = idx.wordIDs
                for (senseid in senseids)  // synset id, sense number, and lemma
                {
                    val sense = this.dict.getWord(senseid)
                    if (sense == null) {
                        System.err.printf("⚠ senseid: %s ➜ null sense", senseid.toString())
                        //val sense2: IWord = this.dict.getWord(senseid);
                        continue
                    }
                    f?.accept(sense)
                }
            }
        }
    }

    fun tryForAllSenses(f: Consumer<IWord>?) {
        for (pos in POS.entries) {
            val it: Iterator<IIndexWord> = dict.getIndexWordIterator(pos)
            while (it.hasNext()) {
                try {
                    val idx = it.next()
                    val senseids: List<IWordID> = idx.wordIDs
                    for (senseid in senseids)  // synset id, sense number, and lemma
                    {
                        val sense = dict.getWord(senseid)
                        if (sense == null) {
                            System.err.printf("⚠ senseid: %s ➜ null sense", senseid.toString())
                            // val sense2: IWord = dict.getWord(senseid)
                            continue
                        }
                        f?.accept(sense)
                    }
                } catch (e: Exception) {
                    System.err.println(e.message)
                }
            }
        }
    }

    fun forAllSynsets(f: Consumer<ISynset>?) {
        for (pos in POS.entries) {
            val it: Iterator<ISynset> = dict.getSynsetIterator(pos)
            while (it.hasNext()) {
                val synset: ISynset = it.next()
                f?.accept(synset)
            }
        }
    }

    fun tryForAllSynsets(f: Consumer<ISynset>?) {
        for (pos in POS.entries) {
            val it: Iterator<ISynset> = dict.getSynsetIterator(pos)
            while (it.hasNext()) {
                try {
                    val synset: ISynset = it.next()
                    f?.accept(synset)
                } catch (e: Exception) {
                    System.err.println(e.message)
                }
            }
        }
    }

    fun forAllSenseEntries(f: Consumer<ISenseEntry>?) {
        val it: Iterator<ISenseEntry> = dict.getSenseEntryIterator()
        while (it.hasNext()) {
            val entry = it.next()
            f?.accept(entry)
        }
    }

    fun tryForAllSenseEntries(f: Consumer<ISenseEntry>?) {
        val it: Iterator<ISenseEntry> = dict.getSenseEntryIterator()
        while (it.hasNext()) {
            try {
                val entry = it.next()
                f?.accept(entry)
            } catch (e: Exception) {
                System.err.println(e.message)
            }
        }
    }

    // S P E C I F I C   I T E R A T I O N S

    fun forAllLemmas(f: Consumer<String?>?) {
        for (pos in POS.entries) {
            val it: Iterator<IIndexWord> = dict.getIndexWordIterator(pos)
            while (it.hasNext()) {
                val idx = it.next()
                val senseids: List<IWordID> = idx.wordIDs
                for (senseid in senseids)  // synset id, sense number, and lemma
                {
                    val sense = dict.getWord(senseid)
                    if (sense == null) {
                        System.err.printf("⚠ senseid: %s ➜ null sense", senseid.toString())
                        // val sense2: IWord = dict.getWord(senseid)
                        continue
                    }
                    val lemma = sense.lemma
                    f?.accept(lemma)
                }
            }
        }
    }

    fun forAllSensekeys(f: Consumer<ISenseKey>?) {
        for (pos in POS.entries) {
            val it: Iterator<IIndexWord> = dict.getIndexWordIterator(pos)
            while (it.hasNext()) {
                val idx = it.next()
                val senseids: List<IWordID> = idx.wordIDs
                for (senseid in senseids)  // synset id, sense number, and lemma
                {
                    val sense = dict.getWord(senseid)
                    if (sense == null) {
                        System.err.printf("⚠ senseid: %s ➜ null sense", senseid.toString())
                        // val sense2: IWord  = dict.getWord(senseid)
                        continue
                    }
                    val sensekey = sense.senseKey
                    f?.accept(sensekey)
                }
            }
        }
    }

    fun forAllSynsetRelations(f: Consumer<ISynset>?) {
        for (pos in POS.entries) {
            val it: Iterator<ISynset> = dict.getSynsetIterator(pos)
            while (it.hasNext()) {
                val synset = it.next()
                val relatedIds: List<ISynsetID> = synset.relatedSynsets
                for (relatedId in relatedIds) {
                    val related = dict.getSynset(relatedId)!!
                    f?.accept(related)
                }
            }
        }
    }

    fun forAllSenseRelations(f: Consumer<IWord>?) {
        for (pos in POS.entries) {
            val it: Iterator<IIndexWord> = dict.getIndexWordIterator(pos)
            while (it.hasNext()) {
                val idx = it.next()
                val senseids: List<IWordID> = idx.wordIDs
                for (senseid in senseids)  // synset id, sense number, and lemma
                {
                    val sense = dict.getWord(senseid)
                    if (sense == null) {
                        System.err.printf("⚠ senseid: %s ➜ null sense", senseid.toString())
                        // val sense2: IWord = dict.getWord(senseid)
                        continue
                    }
                    val relatedIds: List<IWordID>? = sense.relatedWords
                    for (relatedId in relatedIds!!) {
                        val related = dict.getWord(relatedId)!!
                        f?.accept(related)
                    }
                }
            }
        }
    }

    // T R E E   E X P L O R A T I O N S

    fun walk(lemma: String, ps: PrintStream) {
        for (pos in POS.entries) {
            walk(lemma, pos, ps)
        }
    }

    fun walk(lemma: String, pos: POS, ps: PrintStream) {
        // a line in an index file
        val idx = dict.getIndexWord(lemma, pos)
        if (idx != null) {
            // index
            ps.println()
            ps.println("================================================================================")
            ps.println("■ pos = " + pos.name)
            // ps.println("lemma = " + idx.getLemma())
            walk(idx, ps)
        }
    }

    fun walk(idx: IIndexWord, ps: PrintStream) {
        val pointers: Set<IPointer> = idx.pointers
        for (ptr in pointers) {
            ps.println("has relation = $ptr")
        }

        // senseid=(lemma, synsetid, sensenum)
        val senseids: List<IWordID> = idx.wordIDs
        for (senseid in senseids)  // synset id, sense number, and lemma
        {
            walk(senseid, ps)
        }
    }

    fun walk(senseid: IWordID, ps: PrintStream) {
        ps.println("--------------------------------------------------------------------------------")

        //ps.println("senseid = " + senseid.toString())

        // sense=(senseid, lexid, sensekey, synset)
        val sense: IWord? = checkNotNull(dict.getWord(senseid))
        walk(sense!!, ps)

        // synset
        val synsetid = senseid.synsetID
        val synset: ISynset? = checkNotNull(dict.getSynset(synsetid))
        ps.printf("● synset = %s%n", toString(synset!!))

        walk(synset, 1, ps)
    }

    fun walk(sense: IWord, ps: PrintStream) {
        ps.printf("● sense: %s lexid: %d sensekey: %s%n", sense.toString(), sense.lexicalID, sense.senseKey)

        // adj marker
        val marker = sense.adjectiveMarker
        if (marker != null) {
            ps.println("  marker = $marker")
        }

        // sensekey
        val senseKey = sense.senseKey
        val senseEntry = dict.getSenseEntry(senseKey)
        if (senseEntry == null) {
            val synset = checkNotNull(sense.synset)
            val pos: POS? = checkNotNull(sense.pOS)
            System.err.printf("⚠ Missing sensekey %s for sense at offset %d with pos %s%n", senseKey.toString(), synset.offset, pos)
            // throw new IllegalArgumentException(String.format("%s at offset %d with pos %s%n", senseKey.toString(), sense.getSynset().getOffset(),sense.getPOS().toString()))
        }

        // lexical relations
        val relatedMap: Map<IPointer, List<IWordID>> = sense.relatedMap
        walk(relatedMap, ps)

        // verb frames
        val verbFrames: List<IVerbFrame>? = sense.verbFrames
        walk(verbFrames, sense.lemma, ps)

        ps.printf("  sensenum: %s tag cnt:%s%n", senseEntry?.senseNumber ?: "<missing>", senseEntry?.tagCount ?: "<missing>")
    }

    fun walk(relatedMap: Map<IPointer, List<IWordID>>, ps: PrintStream) {
        for (entry in relatedMap.entries) {
            val pointer = entry.key
            for (relatedId in entry.value) {
                val related: IWord? = checkNotNull(dict.getWord(relatedId))
                val relatedSynset = checkNotNull(related!!.synset)
                ps.printf("  related %s lemma:%s synset:%s%n", pointer, related.lemma, relatedSynset)
            }
        }
    }

    fun walk(verbFrames: List<IVerbFrame>?, lemma: String, ps: PrintStream) {
        if (verbFrames != null) {
            for (verbFrame in verbFrames) {
                ps.printf("  verb frame: %s : %s%n", verbFrame.template, verbFrame.instantiateTemplate(lemma))
            }
        }
    }

    fun walk(synset: ISynset, level: Int, ps: PrintStream) {
        val indentSpace = String(CharArray(level)).replace('\u0000', '\t')
        val links: Map<IPointer, List<ISynsetID>> = synset.relatedMap
        for (p in links.keys) {
            ps.printf("%s🡆 %s%n", indentSpace, p.name)
            val relations2: List<ISynsetID> = links[p]!!
            walk(relations2, p, level, ps)
        }
    }

    fun walk(relations2: List<ISynsetID>, p: IPointer, level: Int, ps: PrintStream) {
        val indentSpace = String(CharArray(level)).replace('\u0000', '\t')
        for (synsetid2 in relations2) {
            val synset2: ISynset? = checkNotNull(dict.getSynset(synsetid2))
            ps.printf("%s%s%n", indentSpace, toString(synset2!!))

            walk(synset2, p, level + 1, ps)
        }
    }

    fun walk(synset: ISynset, p: IPointer, level: Int, ps: PrintStream) {
        val indentSpace = String(CharArray(level)).replace('\u0000', '\t')
        val relations2: List<ISynsetID> = checkNotNull(synset.getRelatedSynsets(p))
        for (synsetid2 in relations2) {
            val synset2: ISynset? = checkNotNull(dict.getSynset(synsetid2))
            ps.printf("%s%s%n", indentSpace, toString(synset2!!))
            if (canRecurse(p)) {
                walk(synset2, p, level + 1, ps)
            }
        }
    }

    companion object {

        /**
         * Main
         *
         * @param args arguments
         * @throws IOException io exception
         */
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val wnHome = args[0]
            val lemma = args[1]
            JWI(wnHome).walk(lemma, System.out)
        }

        // H E L P E R S

        fun toString(synset: ISynset): String {
            return getMembers(synset) + synset.gloss
        }

        fun getMembers(synset: ISynset): String {
            val sb = StringBuilder()
            sb.append('{')
            var first = true
            for (sense in synset.words) {
                if (first) {
                    first = false
                } else {
                    sb.append(' ')
                }
                sb.append(sense.lemma)
            }
            sb.append('}')
            sb.append(' ')
            return sb.toString()
        }

        private fun canRecurse(p: IPointer): Boolean {
            val symbol = p.symbol
            when (symbol) {
                "@", "~", "%p", "#p", "%m", "#m", "%s", "#s", "*", ">" -> return true
            }
            return false
        }
    }
}