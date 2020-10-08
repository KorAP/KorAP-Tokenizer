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
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Collection<Object[]> testData = new ArrayList<>();
        for (String encoding : new String[]{"UTF-8", "ISO-8859-1"}) {
            for (int i = 1; true; i++) {
                URL inputUrl = IPCOffsetTests.class.getResource(String.format("/other_test_data/test%02d_input.%s.txt", i, encoding));
                URL goldUrl = IPCOffsetTests.class.getResource(String.format("/other_test_data/test%02d_gold.%s.txt", i, encoding));
                if (inputUrl == null)
                    break;
                testData.add(new String[]{inputUrl.getFile(), goldUrl.getFile(), encoding});
            }
        }
        return testData;
    }

    private final String input;
    private final String gold;
    private final String encoding;

    static String readFile(String path)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public IPCOffsetTests(String input, String gold, String encoding) {
        this.input = input;
        this.gold = gold;
        this.encoding = encoding;
    }

    @Test
    public void testMainWithOffsetsAndSentencesOnDifferentInputFiles() throws IOException {
        File tempFile = File.createTempFile("tokenoutput", ".txt");
        String[] args = {"--encoding", encoding, "--no-tokens", "--positions", "--sentence-boundaries", "--force", "-o", tempFile.getAbsolutePath(), input};
        Main.main(args);
        String actualResult = readFile(tempFile.getAbsolutePath());
        String goldData = readFile(gold);
        assertEquals(goldData, actualResult);
    }
}

