package de.ids_mannheim.korap.tokenizer;

import static org.junit.Assert.*;

import org.apache.maven.surefire.shade.org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.PrintStream;

@RunWith(JUnit4.class)
public class TokenizerTest {

    @Test
    public void testTokenizerSimple () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] tokens = tok.tokenize("Gefunden auf wikipedia.org");
        assertEquals(tokens[0], "Gefunden");
        assertEquals(tokens[1], "auf");
        assertEquals(tokens[2], "wikipedia.org");
        assertEquals(tokens.length, 3);
    }

    @Test
    @Ignore
    public void testTokenizerHost2 () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] tokens = tok.tokenize("Gefunden auf www.wikipedia.org");
        assertEquals(tokens[0], "Gefunden");
        assertEquals(tokens[1], "auf");
        assertEquals(tokens[2], "www.wikipedia.org");
        assertEquals(tokens.length, 3);
    }
    
    @Test
    public void testTokenizerDash () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] tokens = tok.tokenize("Das war -- spitze");
        assertEquals(tokens[0], "Das");
        assertEquals(tokens[1], "war");
        assertEquals(tokens[2], "--");
        assertEquals(tokens[3], "spitze");
        assertEquals(tokens.length, 4);
    }

    @Test
    public void testTokenizerEmail1 () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] tokens = tok.tokenize("Oder unter korap(at)ids-mannheim(dot)de.");
        assertEquals(tokens[0], "Oder");
        assertEquals(tokens[1], "unter");
        assertEquals(tokens[2], "korap(at)ids-mannheim(dot)de");
        assertEquals(tokens[3], ".");
        assertEquals(tokens.length, 4);
    }

    @Test
    public void testTokenizerTwitter () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] tokens = tok.tokenize("Folgt @korap und #korap");
        assertEquals(tokens[0], "Folgt");
        assertEquals(tokens[1], "@korap");
        assertEquals(tokens[2], "und");
        assertEquals(tokens[3], "#korap");
        assertEquals(tokens.length, 4);
    }

    @Test
    public void testTokenizerWeb1 () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] tokens = tok.tokenize("Das ist toll! ;)");
        assertEquals(tokens[0], "Das");
        assertEquals(tokens[1], "ist");
        assertEquals(tokens[2], "toll");
        assertEquals(tokens[3], "!");
        assertEquals(tokens[4], ";)");
        assertEquals(tokens.length, 5);
    }

    @Test
    public void testTokenizerRef1 () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] tokens = tok.tokenize("F*ck!");
        assertEquals(tokens[0], "F*ck");
        assertEquals(tokens[1], "!");
        assertEquals(tokens.length, 2);
    }

    @Test
    public void testTokenizerOmission3 () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] tokens = tok.tokenize("Dieses verf***** Kleid!");
        assertEquals(tokens[0], "Dieses");
        assertEquals(tokens[1], "verf*****");
        assertEquals(tokens[2], "Kleid");
        assertEquals(tokens[3], "!");
        assertEquals(tokens.length, 4);
    }

    @Test
    // Probably interpreted as HOST
    public void testTokenizerFileExtension1 () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] tokens = tok.tokenize("Zeig mir profile.jpeg");
        assertEquals(tokens[0], "Zeig");
        assertEquals(tokens[1], "mir");
        assertEquals(tokens[2], "profile.jpeg");
        assertEquals(tokens.length, 3);
    }

    @Test
    public void testTokenizerFile1 () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] tokens = tok.tokenize("Zeig mir c:\\Dokumente\\profile.docx");
        assertEquals(tokens[0], "Zeig");
        assertEquals(tokens[1], "mir");
        assertEquals(tokens[2], "c:\\Dokumente\\profile.docx");
        assertEquals(tokens.length, 3);
    }

    @Test
    public void testTokenizerFile2 () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] tokens = tok.tokenize("Gehe zu /Dokumente/profile.docx");
        assertEquals(tokens[0], "Gehe");
        assertEquals(tokens[1], "zu");
        assertEquals(tokens[2], "/Dokumente/profile.docx");
        assertEquals(tokens.length, 3);
    }

    @Test
    @Ignore
    public void testTokenizerFile3 () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] tokens = tok.tokenize("Zeig mir c:\\Dokumente\\profile.jpeg");
        assertEquals(tokens[0], "Zeig");
        assertEquals(tokens[1], "mir");
        assertEquals(tokens[2], "c:\\Dokumente\\profile.jpeg");
        assertEquals(tokens.length, 3);
    }

    @Test
    public void testTokenizerPunct () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
    public void testTokenizerPlusAmpersand () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] tokens = tok.tokenize("Siehst Du?!!?");
        assertEquals(tokens[0], "Siehst");
        assertEquals(tokens[1], "Du");
        assertEquals(tokens[2], "?!!?");
        assertEquals(tokens.length, 3);
    }

    @Test
    public void testTokenizerIrishO () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        String[] tokens = tok.tokenize("Peter O'Toole");
        assertEquals(tokens[0], "Peter");
        assertEquals(tokens[1], "O'Toole");
        assertEquals(tokens.length, 2);
    }

    @Test
    public void testTokenizerAbr () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
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
    public void testZipOuputArchive () {
        KorAPTokenizerImpl tok = new KorAPTokenizerImpl();
        final ByteArrayOutputStream clearOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(clearOut));
        String[] tokens = tok.tokenize("Archive:  ich/bin/ein.zip\n");
        assertEquals(0, tokens.length);
    }
}
