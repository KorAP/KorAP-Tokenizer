package de.ids_mannheim.korap.tokenizer;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;

import opennlp.tools.util.Span;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.AssumptionViolatedException;
import org.junit.Assume;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.InvocationTargetException;

@RunWith(JUnit4.class)
public class TokenizerTest {

    @Test
    public void testTokenizerSimple () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Der alte Mann");
        assertEquals(tokens[0], "Der");
        assertEquals(tokens[1], "alte");
        assertEquals(tokens[2], "Mann");
        assertEquals(tokens.length, 3);

        tokens = tok.tokenize("Der alte Mann.");
        assertEquals(tokens[0], "Der");
        assertEquals(tokens[1], "alte");
        assertEquals(tokens[2], "Mann");
        assertEquals(tokens[3], ".");
        assertEquals(tokens.length, 4);
    }

    @Test
    @Ignore
    public void testTokenizerAbbr () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Der Vorsitzende der F.D.P. hat gewählt");
        assertEquals(tokens[0], "Der");
        assertEquals(tokens[1], "Vorsitzende");
        assertEquals(tokens[2], "der");
        assertEquals(tokens[3], "F.D.P.");
        assertEquals(tokens[4], "hat");
        assertEquals(tokens[5], "gewählt");
        assertEquals(tokens.length, 6);
    }    

    @Test
    public void testTokenizerHost1 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Gefunden auf wikipedia.org");
        assertEquals(tokens[0], "Gefunden");
        assertEquals(tokens[1], "auf");
        assertEquals(tokens[2], "wikipedia.org");
        assertEquals(tokens.length, 3);
    }

    @Test
    public void testTokenizerWwwHost () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Gefunden auf www.wikipedia.org");
        assertEquals("Gefunden", tokens[0]);
        assertEquals("auf", tokens[1]);
        assertEquals("www.wikipedia.org", tokens[2]);
        assertEquals(3, tokens.length);
    }

    @Test
    public void testTokenizerWwwUrl () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Weitere Infos unter www.info.biz/info");
        assertEquals("www.info.biz/info", tokens[3]);
    }

    @Ignore
    @Test
    public void testTokenizerFtpHost () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Kann von ftp.download.org heruntergeladen werden");
        assertEquals("Kann", tokens[0]);
        assertEquals("von", tokens[1]);
        assertEquals("ftp.download.org", tokens[2]);
        assertEquals(5, tokens.length);
    }

    @Test
    public void testTokenizerDash () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Das war -- spitze");
        assertEquals(tokens[0], "Das");
        assertEquals(tokens[1], "war");
        assertEquals(tokens[2], "--");
        assertEquals(tokens[3], "spitze");
        assertEquals(tokens.length, 4);
    }

    @Test
    public void testTokenizerEmail1 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Ich bin unter korap@ids-mannheim.de erreichbar.");
        assertEquals(tokens[0], "Ich");
        assertEquals(tokens[1], "bin");
        assertEquals(tokens[2], "unter");
        assertEquals(tokens[3], "korap@ids-mannheim.de");
        assertEquals(tokens[4], "erreichbar");
        assertEquals(tokens[5], ".");
        assertEquals(tokens.length, 6);
    }

    @Test
    public void testTokenizerEmail2 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Oder unter korap[at]ids-mannheim[dot]de.");
        assertEquals(tokens[0], "Oder");
        assertEquals(tokens[1], "unter");
        assertEquals(tokens[2], "korap[at]ids-mannheim[dot]de");
        assertEquals(tokens[3], ".");
        assertEquals(tokens.length, 4);
    }

    @Test
    @Ignore
    public void testTokenizerEmail3 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Oder unter korap(at)ids-mannheim(dot)de.");
        assertEquals(tokens[0], "Oder");
        assertEquals(tokens[1], "unter");
        assertEquals(tokens[2], "korap(at)ids-mannheim(dot)de");
        assertEquals(tokens[3], ".");
        assertEquals(tokens.length, 4);
    }

    @Test
    public void testTokenizerDoNotAcceptQuotedEmailNames () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("\"John Doe\"@xx.com");
        assertEquals("\"", tokens[0]);
        assertEquals("John", tokens[1]);
        assertEquals("Doe", tokens[2]);
        assertEquals("\"", tokens[3]);
        assertEquals("@xx", tokens[4]);
        assertEquals(".", tokens[5]);
        assertEquals("com", tokens[6]);
        assertEquals(7, tokens.length);
    }

    @Test
    public void testTokenizerTwitter () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Folgt @korap und #korap");
        assertEquals(tokens[0], "Folgt");
        assertEquals(tokens[1], "@korap");
        assertEquals(tokens[2], "und");
        assertEquals(tokens[3], "#korap");
        assertEquals(tokens.length, 4);
    }

    @Test
    public void testTokenizerWeb1 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Unsere Website ist https://korap.ids-mannheim.de/?q=Baum");
        assertEquals(tokens[0], "Unsere");
        assertEquals(tokens[1], "Website");
        assertEquals(tokens[2], "ist");
        assertEquals(tokens[3], "https://korap.ids-mannheim.de/?q=Baum");
        assertEquals(tokens.length, 4);
    }

    @Test
    @Ignore
    public void testTokenizerWeb2 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Wir sind auch im Internet (https://korap.ids-mannheim.de/?q=Baum)");
        assertEquals(tokens[0], "Wir");
        assertEquals(tokens[1], "sind");
        assertEquals(tokens[2], "auch");
        assertEquals(tokens[3], "im");
        assertEquals(tokens[4], "Internet");
        assertEquals(tokens[5], "(");
        assertEquals(tokens[6], "https://korap.ids-mannheim.de/?q=Baum");
        assertEquals(tokens[7], ")");
        assertEquals(tokens.length, 8);
    }

    @Test
    @Ignore
    public void testTokenizerWeb3 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Die Adresse ist https://korap.ids-mannheim.de/?q=Baum.");
        assertEquals(tokens[0], "Die");
        assertEquals(tokens[1], "Adresse");
        assertEquals(tokens[2], "ist");
        assertEquals(tokens[3], "https://korap.ids-mannheim.de/?q=Baum");
        assertEquals(tokens[4], ".");
        assertEquals(tokens.length, 8);
    }    

    @Test
    public void testTokenizerServer () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Unser Server ist 10.0.10.51.");
        assertEquals(tokens[0], "Unser");
        assertEquals(tokens[1], "Server");
        assertEquals(tokens[2], "ist");
        assertEquals(tokens[3], "10.0.10.51");
        assertEquals(tokens[4], ".");
        assertEquals(tokens.length, 5);
    }

    @Test
    public void testTokenizerNum () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Zu 50,4% ist es sicher");
        assertEquals(tokens[0], "Zu");
        assertEquals(tokens[1], "50,4");
        assertEquals(tokens[2], "%");  // Arguable
        assertEquals(tokens[3], "ist");
        assertEquals(tokens[4], "es");
        assertEquals(tokens[5], "sicher");
        assertEquals(tokens.length, 6);
    }
    
    @Test
    public void testTokenizerDate () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Der Termin ist am 5.9.2018");
        assertEquals(tokens[0], "Der");
        assertEquals(tokens[1], "Termin");
        assertEquals(tokens[2], "ist");
        assertEquals(tokens[3], "am");
        assertEquals(tokens[4], "5.9.2018");
        assertEquals(tokens.length, 5);

        tokens = tok.tokenize("Der Termin ist am 5/9/2018");
        assertEquals(tokens[0], "Der");
        assertEquals(tokens[1], "Termin");
        assertEquals(tokens[2], "ist");
        assertEquals(tokens[3], "am");
        assertEquals(tokens[4], "5/9/2018");
        assertEquals(tokens.length, 5);
    }
    
    @Test
    @Ignore
    public void testTokenizerDateRange () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Der Termin war vom 4.-5.9.2018");
        assertEquals(tokens[0], "Der");
        assertEquals(tokens[1], "Termin");
        assertEquals(tokens[2], "war");
        assertEquals(tokens[3], "vom");
        assertEquals(tokens[4], "4.");
        assertEquals(tokens[5], "-");
        assertEquals(tokens[6], "5.9.2018");
        assertEquals(tokens.length, 7);
    }

    @Test
    public void testTokenizerEmoji1 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Das ist toll! ;)");
        assertEquals(tokens[0], "Das");
        assertEquals(tokens[1], "ist");
        assertEquals(tokens[2], "toll");
        assertEquals(tokens[3], "!");
        assertEquals(tokens[4], ";)");
        assertEquals(tokens.length, 5);
    }

    // Regression test for https://github.com/KorAP/KorAP-Tokenizer/issues/113
    @Test
    public void testTokenizerEmojiSequences () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        
        // Test emoji with skin tone modifier (U+270A U+1F3FF = raised fist dark skin tone)
        String[] tokens = tok.tokenize("Power ✊🏿!");
        assertEquals("Power", tokens[0]);
        assertEquals("✊🏿", tokens[1]); // Should be one token
        assertEquals("!", tokens[2]);
        assertEquals(3, tokens.length);
        
        // Test emoji ZWJ sequence (family: man, man, boy)
        tokens = tok.tokenize("Familie 👨‍👨‍👦 hier");
        assertEquals("Familie", tokens[0]);
        assertEquals("👨‍👨‍👦", tokens[1]); // Should be one token with ZWJ
        assertEquals("hier", tokens[2]);
        assertEquals(3, tokens.length);
        
        // Test flag emoji (regional indicators for Germany: U+1F1E9 U+1F1EA)
        tokens = tok.tokenize("Flagge 🇩🇪 toll");
        assertEquals("Flagge", tokens[0]);
        assertEquals("🇩🇪", tokens[1]); // Should be one token
        assertEquals("toll", tokens[2]);
        assertEquals(3, tokens.length);
    }

    @Test
    public void testTokenizerRef1 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Kupietz und Schmidt (2018): Korpuslinguistik");
        assertEquals(tokens[0], "Kupietz");
        assertEquals(tokens[1], "und");
        assertEquals(tokens[2], "Schmidt");
        assertEquals(tokens[3], "(");
        assertEquals(tokens[4], "2018");
        assertEquals(tokens[5], ")");
        assertEquals(tokens[6], ":");
        assertEquals(tokens[7], "Korpuslinguistik");
        assertEquals(tokens.length, 8);
    }

    @Test
    public void testTokenizerRef2 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Kupietz und Schmidt [2018]: Korpuslinguistik");
        assertEquals(tokens[0], "Kupietz");
        assertEquals(tokens[1], "und");
        assertEquals(tokens[2], "Schmidt");
        assertEquals(tokens[3], "[");
        assertEquals(tokens[4], "2018");
        assertEquals(tokens[5], "]");
        assertEquals(tokens[6], ":");
        assertEquals(tokens[7], "Korpuslinguistik");
        assertEquals(tokens.length, 8);
    }

    @Test
    public void testTokenizerOmission1 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Er ist ein A****loch!");
        assertEquals(tokens[0], "Er");
        assertEquals(tokens[1], "ist");
        assertEquals(tokens[2], "ein");
        assertEquals(tokens[3], "A****loch");
        assertEquals(tokens[4], "!");
        assertEquals(tokens.length, 5);
    }

    @Test
    public void testTokenizerOmission2 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("F*ck!");
        assertEquals(tokens[0], "F*ck");
        assertEquals(tokens[1], "!");
        assertEquals(tokens.length, 2);
    }

    @Test
    public void testTokenizerOmission3 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Dieses verf***** Kleid!");
        assertEquals(tokens[0], "Dieses");
        assertEquals(tokens[1], "verf*****");
        assertEquals(tokens[2], "Kleid");
        assertEquals(tokens[3], "!");
        assertEquals(tokens.length, 4);
    }

    // Regression test for https://github.com/KorAP/KorAP-Tokenizer/issues/115
    @Test
    public void testTokenizerGendersternAfterHyphen () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Die Serb*innen wie die Kosovo-Albaner*innen");
        assertEquals("Die", tokens[0]);
        assertEquals("Serb*innen", tokens[1]);
        assertEquals("wie", tokens[2]);
        assertEquals("die", tokens[3]);
        assertEquals("Kosovo-Albaner*innen", tokens[4]);
        assertEquals(5, tokens.length);
    }

    // Regression test for https://github.com/KorAP/KorAP-Tokenizer/issues/114
    @Test
    public void testTokenizerWikipediaEmojiTemplate () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        
        // Test Wikipedia emoji template from the issue
        String[] tokens = tok.tokenize("Ein Smiley [_EMOJI:{{S|;)}}_] hier");
        assertEquals("Ein", tokens[0]);
        assertEquals("Smiley", tokens[1]);
        assertEquals("[_EMOJI:{{S|;)}}_]", tokens[2]); // Should be one token
        assertEquals("hier", tokens[3]);
        assertEquals(4, tokens.length);
        
        // Test simple pragma still works
        tokens = tok.tokenize("Name: [_ANONYMIZED_] Ende");
        assertEquals("Name", tokens[0]);
        assertEquals(":", tokens[1]);
        assertEquals("[_ANONYMIZED_]", tokens[2]); // Should be one token
        assertEquals("Ende", tokens[3]);
        assertEquals(4, tokens.length);
    }

    @Test
    // Probably interpreted as HOST
    public void testTokenizerFileExtension1 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Ich habe die readme.txt heruntergeladen");
        assertEquals(tokens[0], "Ich");
        assertEquals(tokens[1], "habe");
        assertEquals(tokens[2], "die");
        assertEquals(tokens[3], "readme.txt");
        assertEquals(tokens[4], "heruntergeladen");
        assertEquals(tokens.length, 5);
    }

    @Test
    // Probably interpreted as HOST
    public void testTokenizerFileExtension2 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Nimm die README.TXT!");
        assertEquals(tokens[0], "Nimm");
        assertEquals(tokens[1], "die");
        assertEquals(tokens[2], "README.TXT");
        assertEquals(tokens[3], "!");
        assertEquals(tokens.length, 4);
    }

    @Test
    // Probably interpreted as HOST
    public void testTokenizerFileExtension3 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Zeig mir profile.jpeg");
        assertEquals(tokens[0], "Zeig");
        assertEquals(tokens[1], "mir");
        assertEquals(tokens[2], "profile.jpeg");
        assertEquals(tokens.length, 3);
    }

    @Test
    public void testTokenizerFile1 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Zeig mir c:\\Dokumente\\profile.docx");
        assertEquals(tokens[0], "Zeig");
        assertEquals(tokens[1], "mir");
        assertEquals(tokens[2], "c:\\Dokumente\\profile.docx");
        assertEquals(tokens.length, 3);
    }

    @Test
    public void testTokenizerFile2 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Gehe zu /Dokumente/profile.docx");
        assertEquals(tokens[0], "Gehe");
        assertEquals(tokens[1], "zu");
        assertEquals(tokens[2], "/Dokumente/profile.docx");
        assertEquals(tokens.length, 3);
    }

    @Test
    public void testTokenizerFile3 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Zeig mir c:\\Dokumente\\profile.jpeg");
        assertEquals(tokens[0], "Zeig");
        assertEquals(tokens[1], "mir");
        assertEquals(tokens[2], "c:\\Dokumente\\profile.jpeg");
        assertEquals(tokens.length, 3);
    }

    @Test
    public void testTokenizerPunct () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Er sagte: \"Es geht mir gut!\", daraufhin ging er.");
        assertEquals(tokens[0], "Er");
        assertEquals(tokens[1], "sagte");
        assertEquals(tokens[2], ":");
        assertEquals(tokens[3], "\"");
        assertEquals(tokens[4], "Es");
        assertEquals(tokens[5], "geht");
        assertEquals(tokens[6], "mir");
        assertEquals(tokens[7], "gut");
        assertEquals(tokens[8], "!");
        assertEquals(tokens[9], "\"");
        assertEquals(tokens[10], ",");
        assertEquals(tokens[11], "daraufhin");
        assertEquals(tokens[12], "ging");
        assertEquals(tokens[13], "er");
        assertEquals(tokens[14], ".");
        assertEquals(tokens.length, 15);
    }

    @Test
    public void testTokenizerPunct2 () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Aber das schreibe ich nur Dir. Innstetten darf nicht davon wissen.");
        assertEquals(tokens[0], "Aber");
        assertEquals(tokens[5], "Dir");
        assertEquals(tokens[6], ".");
        assertEquals(tokens[11], "wissen");
        assertEquals(tokens[12], ".");
        assertEquals(tokens.length, 13);
    }

    @Test
    public void testTokenizerPlusAmpersand () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("&quot;Das ist von C&A!&quot;");
        assertEquals(tokens[0], "&quot;");
        assertEquals(tokens[1], "Das");
        assertEquals(tokens[2], "ist");
        assertEquals(tokens[3], "von");
        assertEquals(tokens[4], "C&A");
        assertEquals(tokens[5], "!");
        assertEquals(tokens[6], "&quot;");
        assertEquals(tokens.length, 7);
    }

    @Test
    public void testTokenizerLongEnd () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Siehst Du?!!?");
        assertEquals(tokens[0], "Siehst");
        assertEquals(tokens[1], "Du");
        assertEquals(tokens[2], "?!!?");
        assertEquals(tokens.length, 3);
    }

    @Test
    public void testTokenizerIrishO () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Peter O'Toole");
        assertEquals(tokens[0], "Peter");
        assertEquals(tokens[1], "O'Toole");
        assertEquals(tokens.length, 2);
    }

    @Test
    public void testTokenizerAbr () {
        Assume.assumeFalse(Boolean.parseBoolean(System.getProperty("force.fast")));
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Früher bzw. später ...");
        assertEquals(tokens[0], "Früher");
        assertEquals(tokens[1], "bzw.");
        assertEquals(tokens[2], "später");
        assertEquals(tokens[3], "...");
        assertEquals(tokens.length, 4);
    }    

    @Test
    @Ignore
    public void testTokenizerUppercaseRule () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Es war spät.Morgen ist es früh.");
        assertEquals(tokens[0], "Es");
        assertEquals(tokens[1], "war");
        assertEquals(tokens[2], "spät");
        assertEquals(tokens[3], ".");
        assertEquals(tokens[4], "Morgen");
        assertEquals(tokens[5], "ist");
        assertEquals(tokens[6], "es");
        assertEquals(tokens[7], "früh");
        assertEquals(tokens[8], ".");
        assertEquals(tokens.length, 9);
    }

    @Test
    public void testTokenizerOrd () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Sie erreichte den 1. Platz!");
        assertEquals(tokens[0], "Sie");
        assertEquals(tokens[1], "erreichte");
        assertEquals(tokens[2], "den");
        assertEquals(tokens[3], "1.");
        assertEquals(tokens[4], "Platz");
        assertEquals(tokens[5], "!");
        assertEquals(tokens.length, 6);
    }

    @Test
    public void testNoZipOuputArchive () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Archive:  Ich bin kein zip\n");
        assertEquals(tokens[0], "Archive");
        assertEquals(tokens[1], ":");
        assertEquals(tokens[2], "Ich");
        assertEquals(tokens[3], "bin");
        assertEquals(tokens[4], "kein");
        assertEquals(tokens[5], "zip");
        assertEquals(6, tokens.length);
    }

    @Test
    public void testTokenizerStrasse () {
        Assume.assumeFalse(Boolean.parseBoolean(System.getProperty("force.fast")));
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Ich wohne in der Weststr. und Du?");
        assertEquals(tokens[4], "Weststr.");
        assertEquals(8, tokens.length);
    }

    @Test
    public void testTokenizerDu () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Ach, da wohnst du.");
        assertEquals(tokens[0], "Ach");
        assertEquals(tokens[1], ",");
        assertEquals(tokens[2], "da");
        assertEquals(tokens[3], "wohnst");
        assertEquals(tokens[4], "du");
        assertEquals(tokens[5], ".");
        assertEquals(6, tokens.length);
    }

    @Test
    public void testTokenizerTime () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Es ist gleich 2:30 Uhr.");
        assertEquals(tokens[0], "Es");
        assertEquals(tokens[1], "ist");
        assertEquals(tokens[2], "gleich");
        assertEquals(tokens[3], "2:30");
        assertEquals(tokens[4], "Uhr");
        assertEquals(tokens[5], ".");
        assertEquals(6, tokens.length);
    }

    @Test
    public void WasteExample () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Am 24.1.1806 feierte E. T. A. Hoffmann seinen 30. Geburtstag.");
        assertEquals(tokens[0], "Am");
        assertEquals(tokens[1], "24.1.1806");
        assertEquals(tokens[2], "feierte");
        assertEquals(tokens[3], "E.");
        assertEquals(tokens[4], "T.");
        assertEquals(tokens[5], "A.");
        assertEquals(tokens[6], "Hoffmann");
        assertEquals(tokens[7], "seinen");
        assertEquals(tokens[8], "30.");
        assertEquals(tokens[9], "Geburtstag");
        assertEquals(tokens[10], ".");
        assertEquals(11, tokens.length);
    }
    
    @Test
    public void germanTokenizerKnowsGermanOmissionWords () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("D'dorf Ku'damm Lu'hafen M'gladbach W'schaft");
        assertEquals("D'dorf", tokens[0]);
        assertEquals("Ku'damm", tokens[1]);
        assertEquals("Lu'hafen", tokens[2]);
        assertEquals("M'gladbach", tokens[3]);
        assertEquals("W'schaft", tokens[4]);
        assertEquals(5, tokens.length);
    }

    @Test
    public void wordsCannotStartWithOmissions () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("'Ddorf 'Kudamm");
        assertEquals("'", tokens[0]);
        assertEquals("Ddorf", tokens[1]);
        assertEquals("'", tokens[2]);
        assertEquals("Kudamm", tokens[3]);
    }

    @Test
    public void germanTokenizerDoesNOTSeparateGermanContractions () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("mach's macht's was'n ist's haste willste kannste biste kriegste");
        assertEquals("mach's", tokens[0]);
        assertEquals("macht's", tokens[1]);
        assertEquals("was'n", tokens[2]);
        assertEquals("ist's", tokens[3]);
        assertEquals("haste", tokens[4]);
        assertEquals("willste", tokens[5]);
        assertEquals("kannste", tokens[6]);
        assertEquals("biste", tokens[7]);
        assertEquals("kriegste", tokens[8]);
        assertEquals(9, tokens.length);
    }

    @Test
    public void englishTokenizerSeparatesEnglishContractionsAndClitics () {
        DerekoDfaTokenizer_en tok = new DerekoDfaTokenizer_en();
        String[] tokens = tok.tokenize("I've we'll you'd I'm we're Peter's isn't");
        assertEquals("'ve", tokens[1]);
        assertEquals("'ll", tokens[3]);
        assertEquals("'d", tokens[5]);
        assertEquals("'m", tokens[7]);
        assertEquals("'re", tokens[9]);
        assertEquals("'s", tokens[11]);
        assertEquals("is", tokens[12]);
        assertEquals("n't", tokens[13]);
        assertEquals(14, tokens.length);
    }

    @Test
    public void frenchTokenizerKnowsFrenchAbbreviations () {
        Assume.assumeFalse(Boolean.parseBoolean(System.getProperty("force.fast")));
        DerekoDfaTokenizer_fr tok = new DerekoDfaTokenizer_fr();
        String[] tokens = tok.tokenize("Approx. en juill. 2004 mon prof. M. Foux m'a dit qu'il faut faire exerc. no. 4, et lire pp. 27-30.");
        assertEquals("Approx.", tokens[0]);
        assertEquals("juill.", tokens[2]);
        assertEquals("prof.", tokens[5]);
        assertEquals("exerc.", tokens[15]);
        assertEquals("no.", tokens[16]);
        assertEquals("pp.", tokens[21]);
    }

    @Test
    public void frenchTokenizerKnowsFrenchContractions () {
        DerekoDfaTokenizer_fr tok = new DerekoDfaTokenizer_fr();
        String[] tokens = tok.tokenize("J'ai j'habite qu'il d'un jusqu'à Aujourd'hui D'accord Quelqu'un Presqu'île");
        assertEquals("J'", tokens[0]);
        assertEquals("j'", tokens[2]);
        assertEquals("qu'", tokens[4]);
        assertEquals("d'", tokens[6]);
        assertEquals("jusqu'", tokens[8]);
        assertEquals("Aujourd'hui", tokens[10]);
        assertEquals("D'", tokens[11]); // ’
        assertEquals("Quelqu'un", tokens[13]); // ’
        assertEquals("Presqu'île", tokens[14]); // ’
    }

    @Test
    public void frenchTokenizerKnowsFrenchClitics () {
        DerekoDfaTokenizer_fr tok = new DerekoDfaTokenizer_fr();
        String[] tokens = tok.tokenize("suis-je sont-elles ");
        assertEquals("suis", tokens[0]);
        assertEquals("-je", tokens[1]);
        assertEquals("sont", tokens[2]);
        assertEquals("-elles", tokens[3]);
    }

    @Test
    public void testEnglishTokenizerScienceAbbreviations () {
        Assume.assumeFalse(Boolean.parseBoolean(System.getProperty("force.fast")));
        DerekoDfaTokenizer_en tok = new DerekoDfaTokenizer_en();
        String[] tokens = tok.tokenize("Approx. in Sept. 1954, Assoc. Prof. Dr. R. J. Ewing reviewed articles on Enzymol. Bacteriol. effects later published in Nutr. Rheumatol. No. 12 and Nº. 13., pp. 17-18.");
        assertEquals("Approx.", tokens[0]);
        assertEquals("in", tokens[1]);
        assertEquals("Sept.", tokens[2]);
        assertEquals("1954", tokens[3]);
        assertEquals(",", tokens[4]);
        assertEquals("Assoc.", tokens[5]);
        assertEquals("Prof.", tokens[6]);
        assertEquals("Dr.", tokens[7]);
        assertEquals("R.", tokens[8]);
        assertEquals("J.", tokens[9]);
        assertEquals("Ewing", tokens[10]);
        assertEquals("reviewed", tokens[11]);
        assertEquals("articles", tokens[12]);
        assertEquals("on", tokens[13]);
        assertEquals("Enzymol.", tokens[14]);
        assertEquals("Bacteriol.", tokens[15]);
        assertEquals("effects", tokens[16]);
        assertEquals("later", tokens[17]);
        assertEquals("published", tokens[18]);
        assertEquals("in", tokens[19]);
        assertEquals("Nutr.", tokens[20]);
        assertEquals("Rheumatol.", tokens[21]);
        assertEquals("No.", tokens[22]);
        assertEquals("12", tokens[23]);
        assertEquals("and", tokens[24]);
        assertEquals("Nº.", tokens[25]);
        assertEquals("13.", tokens[26]);
        assertEquals(",", tokens[27]);
        assertEquals("pp.", tokens[28]);
        assertEquals("17-18", tokens[29]);
        assertEquals(".", tokens[30]);
    }

    @Test
    public void englishTokenizerCanGuessWhetherIIsAbbrev () {
        DerekoDfaTokenizer_en tok = new DerekoDfaTokenizer_en();
        String[] tokens = tok.tokenize("M. I. Baxter was born during World War I. So was I. He went to the Peter I. Hardy school. So did I.");
        assertEquals("I.", tokens[1]);
        assertEquals("I", tokens[8]);
        assertEquals(".", tokens[9]);
        assertEquals("I", tokens[12]);
        assertEquals(".", tokens[13]);
    }

    @Test
    public void testZipOuputArchive () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        final ByteArrayOutputStream clearOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(clearOut));
        String[] tokens = tok.tokenize("Archive:  ich/bin/ein.zip\n");
        assertEquals(0, tokens.length);
    }

    @Test
    public void testTextBreakOutputArchive () throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        DerekoDfaTokenizer_de tok = (DerekoDfaTokenizer_de) new KorapTokenizer.Builder()
                .tokenizerClassName(DerekoDfaTokenizer_de.class.getName())
                .printOffsets(true)
                .build();
        Span[] tokens = tok.tokenizePos("Text1\004\nText2 Hallo\004Rumsdibums\004Das freut mich sehr.\n");
        assertEquals("Text1", tokens[0].getType());
        assertEquals(tokens.length, 9 );
    }

    // Regression test for hyphenated abbreviations from Wiktionary (2024-12)
    @Test
    public void testHyphenatedAbbreviations() {
        Assume.assumeFalse(Boolean.parseBoolean(System.getProperty("force.fast")));
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Ich wohne in Ba.-Wü. und bin Dipl.-Ing. bei Reg.-Bez. Karlsruhe.");
        assertEquals("Ich", tokens[0]);
        assertEquals("wohne", tokens[1]);
        assertEquals("in", tokens[2]);
        assertEquals("Ba.-Wü.", tokens[3]);
        assertEquals("und", tokens[4]);
        assertEquals("bin", tokens[5]);
        assertEquals("Dipl.-Ing.", tokens[6]);
        assertEquals("bei", tokens[7]);
        assertEquals("Reg.-Bez.", tokens[8]);
        assertEquals("Karlsruhe", tokens[9]);
        assertEquals(".", tokens[10]);
        assertEquals(11, tokens.length);
    }

    // Regression test for https://github.com/KorAP/KorAP-Tokenizer/issues/131
    @Test
    public void testSoftHyphensShouldNotSplitWords() {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        // Soft hyphen U+00AD between word parts should not cause token split
        String[] tokens = tok.tokenize("Donau\u00ADdampf\u00ADschiff");
        assertEquals("Donau\u00ADdampf\u00ADschiff", tokens[0]);
        assertEquals(1, tokens.length);
    }

    // Regression tests for German gender-sensitive forms
    @Test
    public void testGenderSensitiveColonForms() {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        
        // Basic colon forms with -in/-innen
        String[] tokens = tok.tokenize("Die Schüler:innen und Lehrer:in kamen.");
        assertEquals("Die", tokens[0]);
        assertEquals("Schüler:innen", tokens[1]);
        assertEquals("und", tokens[2]);
        assertEquals("Lehrer:in", tokens[3]);
        assertEquals("kamen", tokens[4]);
        assertEquals(".", tokens[5]);
        assertEquals(6, tokens.length);
        
        // More colon examples
        tokens = tok.tokenize("Künstler:innen Mitarbeiter:innen Bürger:innen");
        assertEquals("Künstler:innen", tokens[0]);
        assertEquals("Mitarbeiter:innen", tokens[1]);
        assertEquals("Bürger:innen", tokens[2]);
        assertEquals(3, tokens.length);
    }

    @Test
    public void testGenderSensitiveSlashForms() {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        
        // Basic slash forms
        String[] tokens = tok.tokenize("Autor/in Autor/innen Teilnehmer/innen");
        assertEquals("Autor/in", tokens[0]);
        assertEquals("Autor/innen", tokens[1]);
        assertEquals("Teilnehmer/innen", tokens[2]);
        assertEquals(3, tokens.length);
        
        // Slash forms with hyphen: /-in, /-innen, /-frau
        tokens = tok.tokenize("Kaufmann/-frau und Fachmann/-frau");
        assertEquals("Kaufmann/-frau", tokens[0]);
        assertEquals("und", tokens[1]);
        assertEquals("Fachmann/-frau", tokens[2]);
        assertEquals(3, tokens.length);
        
        // Slash forms without hyphen for frau (lowercase only)
        tokens = tok.tokenize("Kaufmann/frau ist auch korrekt.");
        assertEquals("Kaufmann/frau", tokens[0]);
        assertEquals("ist", tokens[1]);
        assertEquals("auch", tokens[2]);
        assertEquals("korrekt", tokens[3]);
        assertEquals(".", tokens[4]);
        assertEquals(5, tokens.length);
    }

    @Test
    public void testGenderSensitiveParentheticalForms() {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        
        // Basic parenthetical forms
        String[] tokens = tok.tokenize("Schüler(innen) und Lehrer(in) kamen.");
        assertEquals("Schüler(innen)", tokens[0]);
        assertEquals("und", tokens[1]);
        assertEquals("Lehrer(in)", tokens[2]);
        assertEquals("kamen", tokens[3]);
        assertEquals(".", tokens[4]);
        assertEquals(5, tokens.length);
    }

    @Test
    public void testGenderSensitiveCompoundWords() {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        
        // Compound words with hyphen + gender ending
        String[] tokens = tok.tokenize("Die Kosovo-Albaner/innen und Kosovo-Albaner:innen trafen sich.");
        assertEquals("Die", tokens[0]);
        assertEquals("Kosovo-Albaner/innen", tokens[1]);
        assertEquals("und", tokens[2]);
        assertEquals("Kosovo-Albaner:innen", tokens[3]);
        assertEquals("trafen", tokens[4]);
        assertEquals("sich", tokens[5]);
        assertEquals(".", tokens[6]);
        assertEquals(7, tokens.length);
        
        // With hyphen: Kosovo-Albaner/-innen
        tokens = tok.tokenize("Kosovo-Albaner/-innen kamen.");
        assertEquals("Kosovo-Albaner/-innen", tokens[0]);
        assertEquals("kamen", tokens[1]);
        assertEquals(".", tokens[2]);
        assertEquals(3, tokens.length);
    }

    @Test
    public void testGenderSensitiveShouldSeparateMannFrau() {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        
        // Mann/Frau should be separated (capital F = standalone word, not suffix)
        String[] tokens = tok.tokenize("Ob Mann/Frau das will?");
        assertEquals("Ob", tokens[0]);
        assertEquals("Mann", tokens[1]);
        assertEquals("/", tokens[2]);
        assertEquals("Frau", tokens[3]);
        assertEquals("das", tokens[4]);
        assertEquals("will", tokens[5]);
        assertEquals("?", tokens[6]);
        assertEquals(7, tokens.length);
        
        // Also Männer/Frauen
        tokens = tok.tokenize("Männer/Frauen sind willkommen.");
        assertEquals("Männer", tokens[0]);
        assertEquals("/", tokens[1]);
        assertEquals("Frauen", tokens[2]);
        assertEquals("sind", tokens[3]);
        assertEquals("willkommen", tokens[4]);
        assertEquals(".", tokens[5]);
        assertEquals(6, tokens.length);
    }

    @Test
    public void testGenderSensitiveSlashFrauOnlyAfterMann() {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        
        // /frau should only be joined when word ends in "mann"
        // "xxx/frau" where xxx doesn't end in "mann" should be SEPARATED
        String[] tokens = tok.tokenize("xxx/frau sollte getrennt sein.");
        assertEquals("xxx", tokens[0]);
        assertEquals("/", tokens[1]);
        assertEquals("frau", tokens[2]);
        assertEquals("sollte", tokens[3]);
        assertEquals("getrennt", tokens[4]);
        assertEquals("sein", tokens[5]);
        assertEquals(".", tokens[6]);
        assertEquals(7, tokens.length);
        
        // But Kaufmann/frau should be one token (word ends in "mann")
        tokens = tok.tokenize("Kaufmann/frau ist ein Beruf.");
        assertEquals("Kaufmann/frau", tokens[0]);
        assertEquals("ist", tokens[1]);
        assertEquals("ein", tokens[2]);
        assertEquals("Beruf", tokens[3]);
        assertEquals(".", tokens[4]);
        assertEquals(5, tokens.length);
        
        // And Fachmann/-frau should be one token
        tokens = tok.tokenize("Fachmann/-frau gesucht");
        assertEquals("Fachmann/-frau", tokens[0]);
        assertEquals("gesucht", tokens[1]);
        assertEquals(2, tokens.length);
        
        // Geschäftsmann/frau should also be one token
        tokens = tok.tokenize("Ein Geschäftsmann/frau wird gesucht.");
        assertEquals("Ein", tokens[0]);
        assertEquals("Geschäftsmann/frau", tokens[1]);
        assertEquals("wird", tokens[2]);
        assertEquals("gesucht", tokens[3]);
        assertEquals(".", tokens[4]);
        assertEquals(5, tokens.length);
    }

    @Test
    public void testGenderSensitiveGenderstern() {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        
        // Genderstern forms (these should already work via existing rules)
        String[] tokens = tok.tokenize("Schüler*innen und Lehrer*innen");
        assertEquals("Schüler*innen", tokens[0]);
        assertEquals("und", tokens[1]);
        assertEquals("Lehrer*innen", tokens[2]);
        assertEquals(3, tokens.length);
    }

    @Test
    public void testGenderSensitiveMixedSentence() {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        
        // Mixed sentence with various gender forms
        String[] tokens = tok.tokenize("Die Schüler:innen, Lehrer/innen und Mitarbeiter(innen) sowie Kaufmann/-frau trafen sich.");
        assertEquals("Die", tokens[0]);
        assertEquals("Schüler:innen", tokens[1]);
        assertEquals(",", tokens[2]);
        assertEquals("Lehrer/innen", tokens[3]);
        assertEquals("und", tokens[4]);
        assertEquals("Mitarbeiter(innen)", tokens[5]);
        assertEquals("sowie", tokens[6]);
        assertEquals("Kaufmann/-frau", tokens[7]);
        assertEquals("trafen", tokens[8]);
        assertEquals("sich", tokens[9]);
        assertEquals(".", tokens[10]);
        assertEquals(11, tokens.length);
    }

    @Test
    public void testGenderSensitiveFormsNotRecognizedInEnglish() {
        DerekoDfaTokenizer_en tok = new DerekoDfaTokenizer_en();
        
        // English tokenizer should NOT recognize German gender-sensitive forms
        // Colon forms should be separated
        String[] tokens = tok.tokenize("Nutzer:innen and Teacher:in test");
        assertEquals("Nutzer", tokens[0]);
        assertEquals(":", tokens[1]);
        assertEquals("innen", tokens[2]);
        
        // Slash forms should be separated
        tokens = tok.tokenize("Nutzer/innen Kaufmann/frau");
        assertEquals("Nutzer", tokens[0]);
        assertEquals("/", tokens[1]);
        assertEquals("innen", tokens[2]);
        assertEquals("Kaufmann", tokens[3]);
        assertEquals("/", tokens[4]);
        assertEquals("frau", tokens[5]);
        assertEquals(6, tokens.length);
    }

    @Test
    public void testGenderSensitiveFormsNotRecognizedInFrench() {
        DerekoDfaTokenizer_fr tok = new DerekoDfaTokenizer_fr();
        
        // French tokenizer should NOT recognize German gender-sensitive forms
        // Colon forms should be separated
        String[] tokens = tok.tokenize("Nutzer:innen et Teacher:in test");
        assertEquals("Nutzer", tokens[0]);
        assertEquals(":", tokens[1]);
        assertEquals("innen", tokens[2]);
        
        // Slash forms should be separated
        tokens = tok.tokenize("Nutzer/innen Kaufmann/frau");
        assertEquals("Nutzer", tokens[0]);
        assertEquals("/", tokens[1]);
        assertEquals("innen", tokens[2]);
        assertEquals("Kaufmann", tokens[3]);
        assertEquals("/", tokens[4]);
        assertEquals("frau", tokens[5]);
        assertEquals(6, tokens.length);
    }

    @Test
    public void testGenderSensitiveFromFile() throws IOException {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        try (InputStream is = getClass().getResourceAsStream("/tokenizer/dontsplit.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] tokens = tok.tokenize(line);
                assertEquals("Should not split: " + line, 1, tokens.length);
                assertEquals("Should match exact string: " + line, line, tokens[0]);
            }
        }
    }

    @Test
    public void testSplitFromFile() throws IOException {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        try (InputStream is = getClass().getResourceAsStream("/tokenizer/split.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] tokens = tok.tokenize(line);
                assertTrue("Should split: " + line, tokens.length > 1);
            }
        }
    }

    // Tests for de_old tokenizer variant (without gender-sensitive rules)
    @Test
    public void testDeOldSplitsGenderColonForms() {
        DerekoDfaTokenizer_de_old tok = new DerekoDfaTokenizer_de_old();
        
        // Colon forms should be separated
        String[] tokens = tok.tokenize("Nutzer:in");
        assertEquals("Nutzer", tokens[0]);
        assertEquals(":", tokens[1]);
        assertEquals("in", tokens[2]);
        assertEquals(3, tokens.length);
        
        tokens = tok.tokenize("Nutzer:innen");
        assertEquals("Nutzer", tokens[0]);
        assertEquals(":", tokens[1]);
        assertEquals("innen", tokens[2]);
        assertEquals(3, tokens.length);
        
        // Short suffix forms with colon should be separated
        tokens = tok.tokenize("dem:r jede:r gute:s");
        assertEquals("dem", tokens[0]);
        assertEquals(":", tokens[1]);
        assertEquals("r", tokens[2]);
        assertEquals("jede", tokens[3]);
        assertEquals(":", tokens[4]);
        assertEquals("r", tokens[5]);
        assertEquals("gute", tokens[6]);
        assertEquals(":", tokens[7]);
        assertEquals("s", tokens[8]);
        assertEquals(9, tokens.length);
    }

    @Test
    public void testDeOldSplitsGenderSlashForms() {
        DerekoDfaTokenizer_de_old tok = new DerekoDfaTokenizer_de_old();
        
        // Slash forms should be separated
        String[] tokens = tok.tokenize("Nutzer/in");
        assertEquals("Nutzer", tokens[0]);
        assertEquals("/", tokens[1]);
        assertEquals("in", tokens[2]);
        assertEquals(3, tokens.length);
        
        tokens = tok.tokenize("Nutzer/innen");
        assertEquals("Nutzer", tokens[0]);
        assertEquals("/", tokens[1]);
        assertEquals("innen", tokens[2]);
        assertEquals(3, tokens.length);
        
        // Kaufmann/frau should be separated
        tokens = tok.tokenize("Kaufmann/frau");
        assertEquals("Kaufmann", tokens[0]);
        assertEquals("/", tokens[1]);
        assertEquals("frau", tokens[2]);
        assertEquals(3, tokens.length);
    }

    @Test
    public void testDeOldSplitsGenderParenForms() {
        DerekoDfaTokenizer_de_old tok = new DerekoDfaTokenizer_de_old();
        
        // Parenthetical forms should be separated
        String[] tokens = tok.tokenize("Nutzer(in)");
        assertEquals("Nutzer", tokens[0]);
        assertEquals("(", tokens[1]);
        assertEquals("in", tokens[2]);
        assertEquals(")", tokens[3]);
        assertEquals(4, tokens.length);
    }

    @Test
    public void testDeOldStillKeepsNonGenderTokens() {
        DerekoDfaTokenizer_de_old tok = new DerekoDfaTokenizer_de_old();
        
        // Regular words should still work
        String[] tokens = tok.tokenize("Der alte Mann");
        assertEquals("Der", tokens[0]);
        assertEquals("alte", tokens[1]);
        assertEquals("Mann", tokens[2]);
        assertEquals(3, tokens.length);
        
        // Compound words with hyphen should be kept together
        tokens = tok.tokenize("Kosovo-Albaner");
        assertEquals("Kosovo-Albaner", tokens[0]);
        assertEquals(1, tokens.length);
    }

    // Regression test for ver.di (German trade union)
    @Test
    public void testVerdiAbbreviation() {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        
        String[] tokens = tok.tokenize("Die Gewerkschaft ver.di fordert mehr Lohn.");
        assertEquals("Die", tokens[0]);
        assertEquals("Gewerkschaft", tokens[1]);
        assertEquals("ver.di", tokens[2]);
        assertEquals("fordert", tokens[3]);
        assertEquals("mehr", tokens[4]);
        assertEquals("Lohn", tokens[5]);
        assertEquals(".", tokens[6]);
        assertEquals(7, tokens.length);
    }
}


