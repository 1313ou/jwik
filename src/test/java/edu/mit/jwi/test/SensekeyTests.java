package edu.mit.jwi.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SensekeyTests
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

    @Test
    public void sensekeysLive()
    {
        try
        {
            TestLib.allSensekeysAreLive(jwi);
        }
        catch (AssertionError ae)
        {
            TestLib.listDeadSensekeys(jwi);
            throw ae;
        }
    }

    @Test
    public void senseEntriesLive()
    {
        TestLib.allSenseEntriesAreLive(jwi);
    }

    @Test
    public void sensekey()
    {
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "galore%5:00:00:abundant:00"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "galore%5:00:00:many:00"));

        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "aborigine%1:18:00::"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "aborigine%1:18:01::"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "Aborigine%1:18:00::"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "Aborigine%1:18:01::"));

        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "hot%3:00:01::"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "hot%3:00:02::"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "hot%5:00:00:active:01"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "hot%5:00:00:charged:00"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "hot%5:00:00:eager:00"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "hot%5:00:00:fast:01"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "hot%5:00:00:fresh:01"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "hot%5:00:00:good:01"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "hot%5:00:00:illegal:00"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "hot%5:00:00:lucky:00"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "hot%5:00:00:near:00"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "hot%5:00:00:new:00"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "hot%5:00:00:popular:00"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "hot%5:00:00:radioactive:00"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "hot%5:00:00:sexy:00"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "hot%5:00:00:skilled:00"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "hot%5:00:00:tasty:00"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "hot%5:00:00:unpleasant:00"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "hot%5:00:00:violent:00"));
        assertTrue(TestLib.sensekeyFromStringIsLive(jwi, "hot%5:00:00:wanted:00"));
    }
}