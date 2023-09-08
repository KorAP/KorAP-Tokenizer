package de.ids_mannheim.korap.tokenizer;

import opennlp.tools.util.Span;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;

public interface KorapTokenizer extends opennlp.tools.tokenize.Tokenizer, opennlp.tools.sentdetect.SentenceDetector {
    /**
     * Scan.
     *
     * @throws java.io.IOException the io exception
     */
    void scan() throws IOException;

    /**
     * Mainly targeted language(s)
     *
     * @return array of ISO 639 alpha-2 or alpha-3 language codes
     * @apiNote will later be used to find appropriate implementations via reflection
     */
    CharSequence[] getTargetLanguages();

    /**
     * Sets input reader.
     *
     * @param inputReader the input reader
     */
    void setInputReader(Reader inputReader);

    /**
     * Switches sentence splitting on or off.
     *
     * @param splitSentences the split sentences flag
     */
    void setSplitSentences(boolean splitSentences);

    /**
     * Switches input echoing on or off.
     *
     * @param echo the echo flag
     */
    void setEcho(boolean echo);

    /**
     * Switches offset printing on or off.
     *
     * @param printOffsets the print offsets
     */
    void setPrintOffsets(boolean printOffsets);

    /**
     * Switches token printing on or off.
     *
     * @param tokenize the tokenize flag
     */
    void setPrintTokens(boolean tokenize);

    /**
     * Sets output stream.
     *
     * @param outputStream the output stream
     */
    void setOutputStream(PrintStream outputStream);

    /**
     * Switches normalization on or off.
     *
     * @param normalize the normalize flag
     */
    void setNormalize(boolean normalize);

    /** {@inheritDoc} */
    String[] tokenize(String s);

    /** {@inheritDoc} */
    Span[] tokenizePos(String s);

    /**
     * Sent detect string [ ].
     *
     * @param s the s
     * @return the string [ ]
     */
    String[] sentDetect(String s);

    Span[] sentPosDetect(String s);

    /**
     * The type Builder.
     */
    class Builder {
        private boolean splitSentences;
        private boolean echo;
        private boolean printOffsets;
        private boolean printTokens;
        private PrintStream outputStream = System.out;
        private boolean normalize;
        private Class tokenizerClass;
        private Reader inputReader;

        /**
         * Tokenizer class name builder.
         *
         * @param tokenizerClassName the tokenizer class name
         * @return the builder
         * @throws ClassNotFoundException the class not found exception
         */
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

        /**
         * Output stream builder.
         *
         * @param outputStream the output stream
         * @return the builder
         */
        public Builder outputStream(PrintStream outputStream) {
            this.outputStream = outputStream;
            return this;
        }

        /**
         * Build korap tokenizer.
         *
         * @return the korap tokenizer
         * @throws IllegalAccessException the illegal access exception
         * @throws InstantiationException the instantiation exception
         */
        public KorapTokenizer build() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
            KorapTokenizer korapTokenizer = (KorapTokenizer) tokenizerClass.getDeclaredConstructor().newInstance();
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
