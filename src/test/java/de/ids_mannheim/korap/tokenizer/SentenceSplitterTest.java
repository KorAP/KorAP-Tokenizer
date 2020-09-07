package de.ids_mannheim.korap.tokenizer;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class SentenceSplitterTest {

    @Test
    public void testSentSplitterSimple () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] sentences = tok.sentDetect("Der alte Mann.");
        assertEquals(sentences.length, 1);
    }

    @Test
    public void testSentSplitterAbbr () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] sentences = tok.sentDetect("Der Vorsitzende der Abk. hat gewählt.");
        assertEquals(sentences.length, 1);
    }

    @Test
    public void testSentSplitterHost1 () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] sentences = tok.sentDetect("Gefunden auf wikipedia.org.");
        assertEquals(sentences.length, 1);
    }

    @Test
    @Ignore
    public void testSentSplitterHost2 () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] sentences = tok.sentDetect("Gefunden auf www.wikipedia.org");
        assertEquals(sentences.length, 1);
    }

    @Test
    public void testSentSplitterEmail1 () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] sentences = tok.sentDetect("Ich bin unter korap@ids-mannheim.de erreichbar.");
        assertEquals(sentences.length, 1);
    }


    @Test
    public void testSentSplitterWeb1 () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] sentences = tok.sentDetect("Unsere Website ist https://korap.ids-mannheim.de/?q=Baum");
        assertEquals(sentences.length, 1);
    }

   @Test
    public void testSentSplitterServer () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] sentences = tok.sentDetect("Unser Server ist 10.0.10.51.");
        assertEquals(sentences.length, 1);
    }

    @Test
    public void testSentSplitterNum () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] sentences = tok.sentDetect("Zu 50.4% ist es sicher");
        assertEquals(sentences.length, 1);
    }

    @Test
    public void testSentSplitterDate () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] sentences = tok.sentDetect("Der Termin ist am 5.9.2018");
        assertEquals(sentences.length, 1);
    }

    @Test
    // Probably interpreted as HOST
    public void testSentSplitterFileExtension1 () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] sentences = tok.sentDetect("Ich habe die readme.txt heruntergeladen");
        assertEquals(sentences.length, 1);
    }

    @Test
    public void testSentMultiMarker () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] sentences = tok.sentDetect("Ausschalten!!! Hast Du nicht gehört???");
        assertEquals("Ausschalten!!!", sentences[0]);
        assertEquals("Hast Du nicht gehört???", sentences[1]);
        assertEquals(sentences.length, 2);
    }

}
