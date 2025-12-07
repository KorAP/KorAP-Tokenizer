package de.ids_mannheim.korap.tokenizer;

import static org.junit.Assert.*;
import java.util.*;
import java.io.*;
import java.net.URLDecoder;
import org.junit.Test;
import org.junit.Assume;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TokenizerCoverTest {

    /**
     * This test suite checks for the tokenization coverage of our
     * tokenizer implementation based on the EmpiriST 2015
     * Gold Standard Suite, published under Creative Commons license
     * BY-SA 3.0.
     *
     * Michael Beißwenger, Sabine Bartsch, Stefan Evert and
     * Kay-Michael Würzner (2016). EmpiriST 2015: A shared task
     * on the automatic linguistic annotation of computer-mediated
     * communication and web corpora. In Proceedings of the 10th
     * Web as Corpus Workshop (WAC-X) and the EmpiriST Shared Task,
     * pages 78–90. Berlin, Germany.
     *
     * https://sites.google.com/site/empirist2015/home/gold
     */

    // Get a data file
    private String getFile (String file) {
        String path = getClass().getResource(file).getFile();

        StringBuilder content = new StringBuilder();
        try {			
			BufferedReader in = new BufferedReader(
				new InputStreamReader(
					new FileInputStream(URLDecoder.decode(path, "UTF-8")),
					"UTF-8"
					)
				);
            String str;
            while ((str = in.readLine()) != null) {
                content.append(str + "\n");
            }
            in.close();
        }
        catch (IOException e) {
            fail(e.getMessage());
        }
        return content.toString();
    }


    /**
     * Scan Empirist articles and iterate through 
     */
    private class EmpiristScanner implements Iterator {
        private Scanner sc;

        public EmpiristScanner (String file) {
            sc = new Scanner(getFile(file));
            sc.useDelimiter("<(?:posting|article)[^>]+?/>");
        }

        // Return next posting/article
        public String next () {
            return sc.next().trim();
        }

        // Check if new posting/article exists
        public boolean hasNext () {
            return sc.hasNext();
        }
    }

    /**
     * To calculate the difference between the gold standard version and
     * our version, we calculate the levenshtein difference between both lists.
     * It's not very intuitive that way, as it does not treat merges and splits
     * specifically (i.e. a merge is one replacement and one deletion, a split
     * is one replacement and one insertion) - so the number is not
     * really meaningful - it's just a way to measure the differences.
     * It's important to note that this differs from the comparison of
     * EmpiriST, where the end boundaries of all tokens are compared.
     */
    public static int levenshteinForStringArrays (String[] s, String[] t) {
      if (s == null || t == null) {
          throw new IllegalArgumentException("Lists must not be null");
      }

      // Code based on Rosettacode.org
      int [] costs = new int[t.length + 1];

      for (int j = 0; j < costs.length; j++)
          costs[j] = j;

      for (int i = 1; i <= s.length; i++) {
          costs[0] = i;
          int nw = i - 1;
          for (int j = 1; j <= t.length; j++) {
              int cj = Math.min(
                  1 + Math.min(costs[j], costs[j - 1]),
                  s[i - 1].equals(t[j - 1]) ? nw : nw + 1
                  );
              nw = costs[j];
              costs[j] = cj;
          }
      }

      return costs[t.length];
    }

    /**
     * Compare the tokenized data of one example file
     * with the gold standard and return the sum of
     * levenshtein distances.
     */
    public int distanceToGoldStandard (DerekoDfaTokenizer_de tok, String suite, String postings) {

        // Load raw postings
        EmpiristScanner esRaw = new EmpiristScanner(
            "/empirist_gold_standard/" + suite + "/raw/" + postings + ".txt"
            );

        // Load tokenized postings
        EmpiristScanner esTokenized = new EmpiristScanner(
            "/empirist_gold_standard/" + suite + "/tokenized/" + postings + ".txt"
            );

        int distance = 0;
        
        // Iterate over all postings
        while (esRaw.hasNext() && esTokenized.hasNext()) {

            // Get the gold standard splitted on new lines
            String [] goldTokens = esTokenized.next().split("\n+");

            // Tokenize the test data
            String [] testTokens = tok.tokenize(esRaw.next());

            if (false) {
                System.err.println("-----------------");
                for (int i = 0; i < Math.min(goldTokens.length, testTokens.length); i++) {
                    System.err.println(goldTokens[i] + " = "+ testTokens[i]);
                }
            }
            
            // Calculate the edit distance of both arrays
            distance += levenshteinForStringArrays(goldTokens, testTokens);
        }

        // Return the sum of all distances
        return distance;
    }


    @Test
    public void testTokenizerCoverEmpiristCmc () {
        Assume.assumeFalse(Boolean.parseBoolean(System.getProperty("force.fast")));

        // Create tokenizer object
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();

        String test = "cmc_test_blog_comment";
        int dist = distanceToGoldStandard(tok, "test_cmc", test);
        assertTrue(test + " = " + dist, dist == 0);

        test = "cmc_test_professional_chat";
        dist = distanceToGoldStandard(tok, "test_cmc", test);
        assertTrue(test + " = " + dist, dist <= 8);

        test = "cmc_test_social_chat";
        dist = distanceToGoldStandard(tok, "test_cmc", test);
        assertTrue(test + " = " + dist, dist <= 23);

        test = "cmc_test_twitter";
        dist = distanceToGoldStandard(tok, "test_cmc", test);
        assertTrue(test + " = " + dist, dist <= 142);

        test = "cmc_test_whatsapp";
        dist = distanceToGoldStandard(tok, "test_cmc", test);
        assertTrue(test + " = " + dist, dist == 0);

        test = "cmc_test_wiki_discussion";
        dist = distanceToGoldStandard(tok, "test_cmc", test);
        assertTrue(test + " = " + dist, dist <= 24);

    }

    @Test
    public void testTokenizerCoverEmpiristWeb () {
        Assume.assumeFalse(Boolean.parseBoolean(System.getProperty("force.fast")));

        // Create tokenizer object
        DerekoDfaTokenizer_de tok = new DerekoDfaTokenizer_de();

        String test = "web_test_001";
        int dist = distanceToGoldStandard(tok, "test_web", test);
        assertTrue(test + " = " + dist, dist <= 21);

        test = "web_test_002";
        dist = distanceToGoldStandard(tok, "test_web", test);
        assertTrue(test + " = " + dist, dist <= 5);

        test = "web_test_003";
        dist = distanceToGoldStandard(tok, "test_web", test);
        assertTrue(test + " = " + dist, dist <= 9);

        test = "web_test_004";
        dist = distanceToGoldStandard(tok, "test_web", test);
        assertTrue(test + " = " + dist, dist == 0);

        test = "web_test_005";
        dist = distanceToGoldStandard(tok, "test_web", test);
        assertTrue(test + " = " + dist, dist <= 2);

        test = "web_test_006";
        dist = distanceToGoldStandard(tok, "test_web", test);
        assertTrue(test + " = " + dist, dist <= 8);

        test = "web_test_007";
        dist = distanceToGoldStandard(tok, "test_web", test);
        assertTrue(test + " = " + dist, dist <= 8);

        test = "web_test_008";
        dist = distanceToGoldStandard(tok, "test_web", test);
        assertTrue(test + " = " + dist, dist <= 2);

        test = "web_test_009";
        dist = distanceToGoldStandard(tok, "test_web", test);
        assertTrue(test + " = " + dist, dist <= 6);

        test = "web_test_010";
        dist = distanceToGoldStandard(tok, "test_web", test);
        assertTrue(test + " = " + dist, dist == 0);

        test = "web_test_011";
        dist = distanceToGoldStandard(tok, "test_web", test);
        assertTrue(test + " = " + dist, dist <= 21);

        test = "web_test_012";
        dist = distanceToGoldStandard(tok, "test_web", test);
        assertTrue(test + " = " + dist, dist <= 7);
    }
}
