package edu.mit.jwi.test;

import edu.mit.jwi.item.POS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JWITests
{
    private static final boolean VERBOSE = !System.getProperties().containsKey("SILENT");

    private static final PrintStream PS = VERBOSE ? System.out : new PrintStream(new OutputStream()
    {
        public void write(int b)
        {
            //DO NOTHING
        }
    });

    private static JWI jwi;

    @BeforeAll
    public static void init() throws IOException
    {
        String wnHome = System.getProperty("SOURCE");
        jwi = new JWI(wnHome);
    }

    // enum

    @Test
    public void allSenses()
    {
        jwi.forAllSenses(null);
    }

    @Test
    public void allSynsets()
    {
        jwi.forAllSynsets(null);
    }

    @Test
    public void allSenseEntries()
    {
        jwi.forAllSenseEntries(null);
    }

    // enum non null

    @Test
    public void allSensesAreNonNull()
    {
        jwi.forAllSenses(Assertions::assertNotNull);
    }

    @Test
    public void allSynsetsAreNonNull()
    {
        jwi.forAllSynsets(Assertions::assertNotNull);
    }

    @Test
    public void allSenseEntriesAreNonNull()
    {
        jwi.forAllSenseEntries(Assertions::assertNotNull);
    }

    @Test
    public void allLemmasAreNonNull()
    {
        jwi.forAllLemmas((l) -> {
            assertNotNull(l);
            assertFalse(l.isEmpty());
        });
    }

    @Test
    public void allSensekeysAreNonNull()
    {
        jwi.forAllSensekeys(Assertions::assertNotNull);
    }

    @Test
    public void allSynsetRelationsAreNonNull()
    {
        jwi.forAllSynsetRelations(Assertions::assertNotNull);
    }

    @Test
    public void allSenseRelationsAreNonNull()
    {
        jwi.forAllSenseRelations(Assertions::assertNotNull);
    }

    // enum live

    @Test
    public void allSensekeysAreLive()
    {
        TestLib.allSensekeysAreLive(jwi);
    }

    @Test
    public void allSenseEntriesAreLive()
    {
        TestLib.allSenseEntriesAreLive(jwi);
    }

    // others

    // the test involves new is_caused_by
    @Test
    public void extraRelations()
    {
        jwi.walk("spread", PS);
    }

    // the test involves Young (n) and adj
    @Test
    public void cased()
    {
        jwi.walk("young", PS);
    }

    // the test involves new is_caused_by
    @Test
    public void cased2()
    {
        jwi.walk("aborigine", PS);
    }

    // the test involves adj
    @Test
    public void adjSat()
    {
        jwi.walk("small", PS);
    }

    // the test involves galore (a)
    @Test
    public void adjMarker()
    {
        jwi.walk("galore", PS);
    }

    // the test involves a frameless entry
    @Test
    public void frameless()
    {
        jwi.getDict().getIndexWord("fangirl", POS.VERB);
        jwi.walk("fangirl", POS.VERB, PS);
    }
}