package de.ids_mannheim.korap.tokenizer;

import static org.junit.Assert.*;

import opennlp.tools.util.Span;
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
    public void IPtest () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("Meine IP ist 192.178.168.55.");
        assertEquals(tokens[0], "Meine");
        assertEquals(tokens[1], "IP");
        assertEquals(tokens[2], "ist");
        assertEquals(tokens[3], "192.178.168.55");
        assertEquals(tokens[4], ".");
        assertEquals(5, tokens.length);
    }

    @Test
    public void CoronaTest () {
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();
        String[] tokens = tok.tokenize("2G+ soll Covid19 verhindern.");
        assertEquals(tokens[0], "2G+");
        assertEquals(tokens[1], "soll");
        assertEquals(tokens[2], "Covid19");
        assertEquals(tokens[3], "verhindern");
        assertEquals(tokens[4], ".");
        assertEquals(5, tokens.length);
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
    public void testTextBreakOutputArchive () throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        DerekoDfaTokenizer_de tok = (DerekoDfaTokenizer_de) new KorapTokenizer.Builder()
                .tokenizerClassName(DerekoDfaTokenizer_de.class.getName())
                .printOffsets(true)
                .build();
        Span[] tokens = tok.tokenizePos("Text1\004\nText2 Hallo\004Rumsdibums\004Das freut mich sehr.\n");
        assertEquals("Text1", tokens[0].getType());
        assertEquals(tokens.length, 9 );
    }
}
