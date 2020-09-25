package de.ids_mannheim.korap.tokenizer;

import opennlp.tools.util.Span;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;

public interface KorapTokenizer extends opennlp.tools.tokenize.Tokenizer, opennlp.tools.sentdetect.SentenceDetector {
    void scan() throws IOException;

    /**
     * Mainly targeted language(s)
     * @return list of ISO 639 alpha-2 or alpha-3 language codes
     * @apiNote will later be used to find appropriate implementations via reflection
     */
    CharSequence[] getTargetLanguages();
    
    void setInputReader(Reader inputReader);

    void setSplitSentences(boolean splitSentences);

    void setEcho(boolean echo);

    void setPrintOffsets(boolean printOffsets);

    void setPrintTokens(boolean tokenize);

    void setOutputStream(PrintStream outputStream);

    void setNormalize(boolean normalize);

    String[] tokenize(String s);

    Span[] tokenizePos(String s);

    String[] sentDetect(String s);

    Span[] sentPosDetect(String s);

    class Builder {
        private boolean splitSentences;
        private boolean echo;
        private boolean printOffsets;
        private boolean printTokens;
        private PrintStream outputStream = System.out;
        private boolean normalize;
        private Class tokenizerClass;
        private Reader inputReader;

        public Builder tokenizerClassName(String tokenizerClassName) throws ClassNotFoundException {
            this.tokenizerClass = Class.forName(tokenizerClassName);
            return this;
        }

        public Builder splitSentences(boolean splitSentences) {
            this.splitSentences = splitSentences;
            return this;
        }

        public Builder setEcho(boolean echo) {
            this.echo = echo;
            return this;
        }

        public Builder printOffsets(boolean printOffsets) {
            this.printOffsets = printOffsets;
            return this;
        }

        public Builder printTokens(boolean printTokens) {
            this.printTokens = printTokens;
            return this;
        }

        public Builder inputReader(Reader inputReader) {
            this.inputReader = inputReader;
            return this;
        }

        public Builder normalize(boolean normalize) {
            this.normalize = normalize;
            return this;
        }

        public Builder outputStream(PrintStream outputStream) {
            this.outputStream = outputStream;
            return this;
        }

        public KorapTokenizer build() throws IllegalAccessException, InstantiationException {
            KorapTokenizer korapTokenizer = (KorapTokenizer) tokenizerClass.newInstance();
            korapTokenizer.setEcho(echo);
            korapTokenizer.setInputReader(inputReader);
            korapTokenizer.setOutputStream(outputStream);
            korapTokenizer.setNormalize(normalize);
            korapTokenizer.setPrintOffsets(printOffsets);
            korapTokenizer.setSplitSentences(splitSentences);
            korapTokenizer.setPrintTokens(printTokens);
            return korapTokenizer;
        }
    }
}
