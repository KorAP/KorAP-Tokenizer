package de.ids_mannheim.korap.tokenizer;

import opennlp.tools.util.Span;
import org.apache.maven.surefire.shade.org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class EnTokenizerTest {

    @Test
    public void testEnglishAbbreviations () {
        DerekoDfaTokenizer_en tok = new DerekoDfaTokenizer_en();
        String[] tokens = tok.tokenize("Mr. B. Otis lives approx. where St. Peter's and Canterbury Rd. cross.");
        assertEquals("Mr.", tokens[0]);
        assertEquals("B.", tokens[1]);
        assertEquals("Otis", tokens[2]);
        assertEquals("lives", tokens[3]);
        assertEquals("approx.", tokens[4]);
        assertEquals("where", tokens[5]);
        assertEquals("St.", tokens[6]);
        assertEquals("Peter's", tokens[7]);
        assertEquals("Rd.", tokens[10]);
        assertEquals("cross", tokens[11]);
    }

}
