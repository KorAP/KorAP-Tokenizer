package de.ids_mannheim.korap.tokenizer;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.Assume;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import opennlp.tools.util.Span;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class SentenceSplitterTest {

    @Test
    public void testSentSplitterSimple () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("Der alte Mann.");
        assertEquals(sentences.length, 1);
    }

    @Test
    public void testSentSplitterAbbr () {
        Assume.assumeFalse(Boolean.parseBoolean(System.getProperty("force.fast")));
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("Der Vorsitzende der Abk. hat gewählt.");
        assertEquals(sentences.length, 1);
    }

    @Test
    public void testSentSplitterHost1 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("Gefunden auf wikipedia.org.");
        assertEquals(sentences.length, 1);
    }

    @Test
    public void testSentSplitterHost2 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("Gefunden auf www.wikipedia.org");
        assertEquals(sentences.length, 1);
    }

    @Test
    public void testSentSplitterEmail1 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("Ich bin unter korap@ids-mannheim.de erreichbar.");
        assertEquals(sentences.length, 1);
    }

    @Test
    public void testSentSplitterWeb1 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("Unsere Website ist https://korap.ids-mannheim.de/?q=Baum");
        assertEquals(sentences.length, 1);
    }

    @Test
    public void testSentSplitterServer () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("Unser Server ist 10.0.10.51.");
        assertEquals(sentences.length, 1);
    }

    @Test
    public void testSentSplitterNum () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("Zu 50.4% ist es sicher");
        assertEquals(sentences.length, 1);
    }

    @Test
    public void testSentSplitterDate () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("Der Termin ist am 5.9.2018");
        assertEquals(sentences.length, 1);
    }

    @Test
    // Probably interpreted as HOST
    public void testSentSplitterFileExtension1 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("Ich habe die readme.txt heruntergeladen");
        assertEquals(sentences.length, 1);
    }

    @Test
    public void testSentMultiMarker () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("Ausschalten!!! Hast Du nicht gehört???");
        assertEquals("Ausschalten!!!", sentences[0]);
        assertEquals("Hast Du nicht gehört???", sentences[1]);
        assertEquals(sentences.length, 2);
    }

    @Test
    public void testSentSplitterStrasse () {
        Assume.assumeFalse(Boolean.parseBoolean(System.getProperty("force.fast")));
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("Ich wohne in der Weststr. und Du?");
        assertEquals(sentences.length, 1);
    }

    @Test
    @Ignore
    public void testSentSplitterQuote () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("\"Ausschalten!!!\", sagte er. \"Hast Du nicht gehört???\"");
        assertEquals("\"Ausschalten!!!\", sagte er.", sentences[0]);
        assertEquals("\"Hast Du nicht gehört???\"", sentences[1]);
        assertEquals(sentences.length, 2);
    }

    @Test
    public void testSentSplitterGermanNestedQuotes () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String input = "Worum es gehe, erkundigte sich Nicole bei einem der Krankenpfleger. „Was ist mit dem Baby?“ „Blutdruck normal, 110 zu 75. Puls 62, leicht sinkend …“ Während der Sanitäter die Daten im Telegrammstil wiedergab, sah Nicole auf die bewusstlose Patientin hinunter. Ihr Gesicht,";

        String[] sentences = tok.sentDetect(input);

        assertEquals(5, sentences.length);
        assertEquals("Worum es gehe, erkundigte sich Nicole bei einem der Krankenpfleger.", sentences[0]);
        assertEquals("„Was ist mit dem Baby?“", sentences[1]);
        assertEquals("„Blutdruck normal, 110 zu 75. Puls 62, leicht sinkend …“", sentences[2]);
        assertEquals("Während der Sanitäter die Daten im Telegrammstil wiedergab, sah Nicole auf die bewusstlose Patientin hinunter.", sentences[3]);
        assertEquals("Ihr Gesicht,", sentences[4]);
    }

    @Test
    public void testSentSplitterGermanQuoteWithPeriod () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("„Das ist gut.“ Er ging.");
        assertEquals(2, sentences.length);
        assertEquals("„Das ist gut.“", sentences[0]);
        assertEquals("Er ging.", sentences[1]);
    }

    @Test
    public void testSentSplitterGermanQuoteWithExclamation () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("„Halt!“ Er blieb stehen.");
        assertEquals(2, sentences.length);
        assertEquals("„Halt!“", sentences[0]);
        assertEquals("Er blieb stehen.", sentences[1]);
    }

    @Test
    public void testSentSplitterGermanQuoteWithQuestion () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("„Wirklich?“ Sie nickte.");
        assertEquals(2, sentences.length);
        assertEquals("„Wirklich?“", sentences[0]);
        assertEquals("Sie nickte.", sentences[1]);
    }

    @Test
    public void testSentSplitterGermanQuoteWithEllipsis () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("„Na ja …“ Er schwieg.");
        assertEquals(2, sentences.length);
        assertEquals("„Na ja …“", sentences[0]);
        assertEquals("Er schwieg.", sentences[1]);
    }

    @Test
    public void testSentSplitterEllipsisWithoutQuote () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("Na ja … Er schwieg.");
        assertEquals(2, sentences.length);
        assertEquals("Na ja …", sentences[0]);
        assertEquals("Er schwieg.", sentences[1]);
    }

    @Test
    public void testSentSplitterEnglishClosingQuote () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("He said “Hello.” She left.");
        assertEquals(2, sentences.length);
        assertEquals("He said “Hello.”", sentences[0]);
        assertEquals("She left.", sentences[1]);
    }

    @Test
    public void testSentSplitterNoFalseQuoteAbsorption () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("Er sagte nichts. „Wirklich?“");
        assertEquals(2, sentences.length);
        assertEquals("Er sagte nichts.", sentences[0]);
        assertEquals("„Wirklich?“", sentences[1]);
    }

    @Test
    public void testSentSplitterMultipleGermanQuotedSentences () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("„Ja.“ „Nein.“ „Vielleicht.“");
        assertEquals(3, sentences.length);
        assertEquals("„Ja.“", sentences[0]);
        assertEquals("„Nein.“", sentences[1]);
        assertEquals("„Vielleicht.“", sentences[2]);
    }

    @Test
    public void testSentSplitterGermanQuotePosDetect () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        Span[] spans = tok.sentPosDetect("„Halt!“ Er ging.");
        assertEquals(2, spans.length);
        assertEquals(0, spans[0].getStart());
        assertEquals(7, spans[0].getEnd());
        assertEquals("„Halt!“", spans[0].getType());
        assertEquals(8, spans[1].getStart());
        assertEquals(16, spans[1].getEnd());
        assertEquals("Er ging.", spans[1].getType());
    }

    @Test
    public void testSentSplitterGermanOpeningQuoteTokenized () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("„Hallo“");
        assertEquals(3, tokens.length);
        assertEquals("„", tokens[0]);
        assertEquals("Hallo", tokens[1]);
        assertEquals("“", tokens[2]);
    }

    @Test
    public void testSentSplitterSimpleUnchanged () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] sentences = tok.sentDetect("Erste. Zweite. Dritte.");
        assertEquals(3, sentences.length);
        assertEquals("Erste.", sentences[0]);
        assertEquals("Zweite.", sentences[1]);
        assertEquals("Dritte.", sentences[2]);
    }
}
