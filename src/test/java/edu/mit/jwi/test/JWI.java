package edu.mit.jwi.test;

import edu.mit.jwi.*;
import edu.mit.jwi.item.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * JWI
 *
 * @author Bernard Bou
 */
public class JWI
{
    @NonNull
    private final IDictionary dict;

    /**
     * Main
     *
     * @param args arguments
     * @throws IOException io exception
     */
    public static void main(final String[] args) throws IOException
    {
        final String wnHome = args[0];
        final String lemma = args[1];
        new JWI(wnHome).walk(lemma, System.out);
    }

    public JWI(@NonNull final String wnHome) throws IOException
    {
        this(wnHome, null);
    }

    /**
     * Constructor
     *
     * @param wnHome wordnet home
     * @param config config
     *               final Config config = new Config();
     *               config.checkLexicalId = false;
     *               config.charSet = StandardCharsets.UTF_8;
     *               config.indexSenseKeyComparator = Comparators.CaseSensitiveSenseKeyLineComparator.getInstance();
     * @throws IOException io exception
     */
    public JWI(@NonNull final String wnHome, @Nullable final Config config) throws IOException
    {
        System.out.printf("FROM %s%n", wnHome);
        System.out.printf("CONFIG %s%n", config);

        // construct the URL to the WordNet dictionary directory
        URL url = new File(wnHome).toURI().toURL();

        // construct the dictionary object and open it
        this.dict = new Dictionary(url, config);

        // open it
        this.dict.open();
    }

    @NonNull
    public IDictionary getDict()
    {
        return dict;
    }

    // M A I N   I T E R A T I O N S

    public void forAllSenses(@Nullable final Consumer<IWord> f)
    {
        for (final POS pos : POS.values())
        {
            Iterator<IIndexWord> it = this.dict.getIndexWordIterator(pos);
            while (it.hasNext())
            {
                IIndexWord idx = it.next();
                final List<IWordID> senseids = idx.getWordIDs();
                for (final IWordID senseid : senseids) // synset id, sense number, and lemma
                {
                    IWord sense = this.dict.getWord(senseid);
                    if (sense == null)
                    {
                        System.err.printf("⚠ senseid: %s ➜ null sense", senseid.toString());
                        //IWord sense2 = this.dict.getWord(senseid);
                        continue;
                    }
                    if (f != null)
                    {
                        f.accept(sense);
                    }
                }
            }
        }
    }

    public void tryForAllSenses(@Nullable final Consumer<IWord> f)
    {
        for (final POS pos : POS.values())
        {
            Iterator<IIndexWord> it = this.dict.getIndexWordIterator(pos);
            while (it.hasNext())
            {
                try
                {
                    IIndexWord idx = it.next();
                    final List<IWordID> senseids = idx.getWordIDs();
                    for (final IWordID senseid : senseids) // synset id, sense number, and lemma
                    {
                        IWord sense = this.dict.getWord(senseid);
                        if (sense == null)
                        {
                            System.err.printf("⚠ senseid: %s ➜ null sense", senseid.toString());
                            //IWord sense2 = this.dict.getWord(senseid);
                            continue;
                        }
                        if (f != null)
                        {
                            f.accept(sense);
                        }
                    }
                }
                catch (Exception e)
                {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    public void forAllSynsets(@Nullable final Consumer<ISynset> f)
    {
        for (final POS pos : POS.values())
        {
            Iterator<ISynset> it = this.dict.getSynsetIterator(pos);
            while (it.hasNext())
            {
                ISynset synset = it.next();
                if (f != null)
                {
                    f.accept(synset);
                }
            }
        }
    }

    public void tryForAllSynsets(@Nullable final Consumer<ISynset> f)
    {
        for (final POS pos : POS.values())
        {
            Iterator<ISynset> it = this.dict.getSynsetIterator(pos);
            while (it.hasNext())
            {
                try
                {
                    ISynset synset = it.next();
                    if (f != null)
                    {
                        f.accept(synset);
                    }
                }
                catch (Exception e)
                {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    public void forAllSenseEntries(@Nullable final Consumer<ISenseEntry> f)
    {
        Iterator<ISenseEntry> it = this.dict.getSenseEntryIterator();
        while (it.hasNext())
        {
            ISenseEntry entry = it.next();
            if (f != null)
            {
                f.accept(entry);
            }
        }
    }

    public void tryForAllSenseEntries(@Nullable final Consumer<ISenseEntry> f)
    {
        Iterator<ISenseEntry> it = this.dict.getSenseEntryIterator();
        while (it.hasNext())
        {
            try
            {
                ISenseEntry entry = it.next();
                if (f != null)
                {
                    f.accept(entry);
                }
            }
            catch (Exception e)
            {
                System.err.println(e.getMessage());
            }
        }
    }

    // S P E C I F I C   I T E R A T I O N S

    public void forAllLemmas(@Nullable final Consumer<String> f)
    {
        for (final POS pos : POS.values())
        {
            Iterator<IIndexWord> it = this.dict.getIndexWordIterator(pos);
            while (it.hasNext())
            {
                IIndexWord idx = it.next();
                final List<IWordID> senseids = idx.getWordIDs();
                for (final IWordID senseid : senseids) // synset id, sense number, and lemma
                {
                    IWord sense = this.dict.getWord(senseid);
                    if (sense == null)
                    {
                        System.err.printf("⚠ senseid: %s ➜ null sense", senseid.toString());
                        // IWord sense2 = this.dict.getWord(senseid);
                        continue;
                    }
                    String lemma = sense.getLemma();
                    if (f != null)
                    {
                        f.accept(lemma);
                    }
                }
            }
        }
    }

    public void forAllSensekeys(@Nullable final Consumer<ISenseKey> f)
    {
        for (final POS pos : POS.values())
        {
            Iterator<IIndexWord> it = this.dict.getIndexWordIterator(pos);
            while (it.hasNext())
            {
                IIndexWord idx = it.next();
                final List<IWordID> senseids = idx.getWordIDs();
                for (final IWordID senseid : senseids) // synset id, sense number, and lemma
                {
                    IWord sense = this.dict.getWord(senseid);
                    if (sense == null)
                    {
                        System.err.printf("⚠ senseid: %s ➜ null sense", senseid.toString());
                        //IWord sense2 = this.dict.getWord(senseid);
                        continue;
                    }
                    ISenseKey sensekey = sense.getSenseKey();
                    if (f != null)
                    {
                        f.accept(sensekey);
                    }
                }
            }
        }
    }

    public void forAllSynsetRelations(@Nullable final Consumer<ISynset> f)
    {
        for (final POS pos : POS.values())
        {
            Iterator<ISynset> it = this.dict.getSynsetIterator(pos);
            while (it.hasNext())
            {
                ISynset synset = it.next();
                List<ISynsetID> relatedIds = synset.getRelatedSynsets();
                for (ISynsetID relatedId : relatedIds)
                {
                    ISynset related = this.dict.getSynset(relatedId);
                    if (f != null)
                    {
                        f.accept(related);
                    }
                }
            }
        }
    }

    public void forAllSenseRelations(@Nullable final Consumer<IWord> f)
    {
        for (final POS pos : POS.values())
        {
            Iterator<IIndexWord> it = this.dict.getIndexWordIterator(pos);
            while (it.hasNext())
            {
                IIndexWord idx = it.next();
                final List<IWordID> senseids = idx.getWordIDs();
                for (final IWordID senseid : senseids) // synset id, sense number, and lemma
                {
                    IWord sense = this.dict.getWord(senseid);
                    if (sense == null)
                    {
                        System.err.printf("⚠ senseid: %s ➜ null sense", senseid.toString());
                        //IWord sense2 = this.dict.getWord(senseid);
                        continue;
                    }
                    List<IWordID> relatedIds = sense.getRelatedWords();
                    for (IWordID relatedId : relatedIds)
                    {
                        IWord related = this.dict.getWord(relatedId);
                        if (f != null)
                        {
                            f.accept(related);
                        }
                    }
                }
            }
        }
    }

    // T R E E   E X P L O R A T I O N S

    public void walk(final String lemma, final PrintStream ps)
    {
        for (final POS pos : POS.values())
        {
            walk(lemma, pos, ps);
        }
    }

    public void walk(final String lemma, @NonNull final POS pos, final PrintStream ps)
    {
        // a line in an index file
        final IIndexWord idx = this.dict.getIndexWord(lemma, pos);
        if (idx != null)
        {
            // index
            ps.println();
            ps.println("================================================================================");
            ps.println("■ pos = " + pos.name());
            // ps.println("lemma = " + idx.getLemma());
            walk(idx, ps);
        }
    }

    public void walk(@NonNull final IIndexWord idx, final PrintStream ps)
    {
        Set<IPointer> pointers = idx.getPointers();
        for (IPointer ptr : pointers)
        {
            ps.println("has relation = " + ptr.toString());
        }

        // senseid=(lemma, synsetid, sensenum)
        final List<IWordID> senseids = idx.getWordIDs();
        for (final IWordID senseid : senseids) // synset id, sense number, and lemma
        {
            walk(senseid, ps);
        }
    }

    public void walk(@NonNull final IWordID senseid, final PrintStream ps)
    {
        ps.println("--------------------------------------------------------------------------------");
        //ps.println("senseid = " + senseid.toString());

        // sense=(senseid, lexid, sensekey, synset)
        IWord sense = this.dict.getWord(senseid);
        assert sense != null;
        walk(sense, ps);

        // synset
        final ISynsetID synsetid = senseid.getSynsetID();
        final ISynset synset = this.dict.getSynset(synsetid);
        assert synset != null;
        ps.printf("● synset = %s%n", toString(synset));

        walk(synset, 1, ps);
    }

    public void walk(@NonNull final IWord sense, final PrintStream ps)
    {
        ps.printf("● sense: %s lexid: %d sensekey: %s%n", sense.toString(), sense.getLexicalID(), sense.getSenseKey());

        // adj marker
        AdjMarker marker = sense.getAdjectiveMarker();
        if (marker != null)
        {
            ps.println("  marker = " + marker);
        }

        // sensekey
        ISenseKey senseKey = sense.getSenseKey();
        ISenseEntry senseEntry = this.dict.getSenseEntry(senseKey);
        if (senseEntry == null)
        {
            ISynset synset = sense.getSynset();
            assert synset != null;
            POS pos = sense.getPOS();
            assert pos != null;
            System.err.printf("⚠ Missing sensekey %s for sense at offset %d with pos %s%n", senseKey.toString(), synset.getOffset(), pos);
            // throw new IllegalArgumentException(String.format("%s at offset %d with pos %s%n", senseKey.toString(), sense.getSynset().getOffset(),sense.getPOS().toString()));
        }

        // lexical relations
        Map<IPointer, List<IWordID>> relatedMap = sense.getRelatedMap();
        walk(relatedMap, ps);

        // verb frames
        List<IVerbFrame> verbFrames = sense.getVerbFrames();
        walk(verbFrames, sense.getLemma(), ps);

        ps.printf("  sensenum: %s tag cnt:%s%n", senseEntry == null ? "<missing>" : senseEntry.getSenseNumber(), senseEntry == null ? "<missing>" : senseEntry.getTagCount());
    }

    public void walk(@Nullable final Map<IPointer, List<IWordID>> relatedMap, final PrintStream ps)
    {
        if (relatedMap != null)
        {
            for (Map.Entry<IPointer, List<IWordID>> entry : relatedMap.entrySet())
            {
                IPointer pointer = entry.getKey();
                for (IWordID relatedId : entry.getValue())
                {
                    IWord related = this.dict.getWord(relatedId);
                    assert related != null;
                    ISynset relatedSynset = related.getSynset();
                    assert relatedSynset != null;
                    ps.printf("  related %s lemma:%s synset:%s%n", pointer, related.getLemma(), relatedSynset);
                }
            }
        }
    }

    public void walk(@Nullable final List<IVerbFrame> verbFrames, final String lemma, final PrintStream ps)
    {
        if (verbFrames != null)
        {
            for (IVerbFrame verbFrame : verbFrames)
            {
                ps.printf("  verb frame: %s : %s%n", verbFrame.getTemplate(), verbFrame.instantiateTemplate(lemma));
            }
        }
    }

    public void walk(@NonNull final ISynset synset, final int level, final PrintStream ps)
    {
        final String indentSpace = new String(new char[level]).replace('\0', '\t');
        final Map<IPointer, List<ISynsetID>> links = synset.getRelatedMap();
        for (final IPointer p : links.keySet())
        {
            ps.printf("%s🡆 %s%n", indentSpace, p.getName());
            final List<ISynsetID> relations2 = links.get(p);
            walk(relations2, p, level, ps);
        }
    }

    public void walk(@NonNull final List<ISynsetID> relations2, @NonNull final IPointer p, final int level, final PrintStream ps)
    {
        final String indentSpace = new String(new char[level]).replace('\0', '\t');
        for (final ISynsetID synsetid2 : relations2)
        {
            final ISynset synset2 = this.dict.getSynset(synsetid2);
            assert synset2 != null;
            ps.printf("%s%s%n", indentSpace, toString(synset2));

            walk(synset2, p, level + 1, ps);
        }
    }

    public void walk(@NonNull final ISynset synset, @NonNull final IPointer p, final int level, final PrintStream ps)
    {
        final String indentSpace = new String(new char[level]).replace('\0', '\t');
        final List<ISynsetID> relations2 = synset.getRelatedSynsets(p);
        assert relations2 != null;
        for (final ISynsetID synsetid2 : relations2)
        {
            final ISynset synset2 = this.dict.getSynset(synsetid2);
            assert synset2 != null;
            ps.printf("%s%s%n", indentSpace, toString(synset2));
            if (canRecurse(p))
            {
                walk(synset2, p, level + 1, ps);
            }
        }
    }

    // H E L P E R S

    @NonNull
    public static String toString(@NonNull final ISynset synset)
    {
        return getMembers(synset) + synset.getGloss();
    }

    @NonNull
    public static String getMembers(@NonNull final ISynset synset)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (final IWord sense : synset.getWords())
        {
            if (first)
            {
                first = false;
            }
            else
            {
                sb.append(' ');
            }
            sb.append(sense.getLemma());
        }
        sb.append('}');
        sb.append(' ');
        return sb.toString();
    }

    private static boolean canRecurse(@NonNull IPointer p)
    {
        String symbol = p.getSymbol();
        switch (symbol)
        {
            case "@": // hypernym
            case "~": // hyponym
            case "%p": // part holonym
            case "#p": // part meronym
            case "%m": // member holonym
            case "#m": // member meronym
            case "%s": // substance holonym
            case "#s": // substance meronym
            case "*": // entail
            case ">": // cause
                return true;
        }
        return false;
    }
}
