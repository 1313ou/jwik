package edu.mit.jwi.test;

import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.PrintStream;

public class LexidTests
{
    private static final boolean VERBOSE = !System.getProperties().containsKey("SILENT");

    private static final PrintStream PS = VERBOSE ? System.out : new PrintStream(new OutputStream()
    {
        public void write(int b)
        {
            //DO NOTHING
        }
    });

    @Test
    public void parseCompatLexid()
    {
        String line = "02504828 00 s 01 hot 03 001 & 02504619 a 0000 | (color) bold and intense; \"hot pink\"";
        TestLib.parseDataLineIntoMembers(line).forEach(PS::println);
    }

    @Test
    public void parseNonCompatLexid()
    {
        String line = "02504828 00 s 01 hot 13 001 & 02504619 a 0000 | (color) bold and intense; \"hot pink\"";
        TestLib.parseDataLineIntoMembers(line).forEach(PS::println);
    }
}