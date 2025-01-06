package edu.mit.jwi.test;

import edu.mit.jwi.item.POS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Set;

public class JWIWordStartTests
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
    private static String start;
    private static POS pos;

    @BeforeAll
    public static void init() throws IOException
    {
        String wnHome = System.getProperty("SOURCE");
        jwi = new JWI(wnHome);

        start = System.getProperty("TARGET");

        String scope = System.getProperty("TARGETSCOPE");
        try
        {
            pos = POS.valueOf(scope);
        }
        catch (IllegalArgumentException e)
        {
            pos = null;
        }
    }

    @Test
    public void searchStart()
    {
        Set<String> result = jwi.getDict().getWords(start, pos, 0);
        PS.println(start);
        PS.println(result);
    }

    @Test
    public void searchStartLimited()
    {
        Set<String> result = jwi.getDict().getWords(start, pos, 3);
        PS.println(start);
        PS.println(result);
    }
}