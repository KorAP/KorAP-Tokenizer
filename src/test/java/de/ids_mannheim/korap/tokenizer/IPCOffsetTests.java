package de.ids_mannheim.korap.tokenizer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class IPCOffsetTests {

    static final String testFiletemplate = "/other_test_data/test.%s.%s.%02d.%s.txt";
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Collection<Object[]> testData = new ArrayList<>();
        for (String language : new String[]{"de", "en", "fr"}) {
            for (String encoding : new String[]{"ascii", "latin1", "utf8"}) {
                for (int i = 1; true; i++) {
                    URL inputUrl = IPCOffsetTests.class.getResource(String.format(testFiletemplate, language, encoding, i, "input"));
                    URL positionsUrl = IPCOffsetTests.class.getResource(String.format(testFiletemplate, language, encoding, i, "positions"));
                    URL tokensUrl = IPCOffsetTests.class.getResource(String.format(testFiletemplate, language, encoding, i, "tokens"));
                    if (inputUrl == null)
                        break;
                    testData.add(new String[]{inputUrl.getFile(), positionsUrl.getFile(), tokensUrl.getFile(), language, encoding});
                }
            }
        }
        return testData;
    }

    private final String input;
    private final String positions;
    private final String tokens;
    private final String encoding;
    private final String language;

    static String readFile(String path)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public IPCOffsetTests(String input, String positions, String tokens, String language, String encoding) {
        this.input = input;
        this.positions = positions;
        this.tokens = tokens;
        this.language = language;
        this.encoding = encoding;
    }

    @Test
    public void testMainWithOffsetsAndSentencesOnDifferentInputFiles() throws IOException {
        File tempFile = File.createTempFile("position_output", ".txt");
        String[] args = {"--language", language, "--encoding", encoding, "--no-tokens", "--positions", "--sentence-boundaries", "--force", "-o", tempFile.getAbsolutePath(), input};
        Main.main(args);
        String actualResult = readFile(tempFile.getAbsolutePath());
        String goldData = readFile(positions);
        assertEquals("Testing "+tempFile+ " against " + new File(positions).getName(), goldData, actualResult);
    }

    @Test
    public void testMainWithTokenOutputOnDifferentInputFiles() throws IOException {
        File tempFile = File.createTempFile("token_output", ".txt");
        String[] args = {"--language", language, "--encoding", encoding, "--tokens", "--force", "-o", tempFile.getAbsolutePath(), input};
        Main.main(args);
        String actualResult = readFile(tempFile.getAbsolutePath());
        String goldData = readFile(tokens);
        assertEquals("Testing " + tempFile + " against " + new File(tokens).getName(), goldData, actualResult);
    }
}

