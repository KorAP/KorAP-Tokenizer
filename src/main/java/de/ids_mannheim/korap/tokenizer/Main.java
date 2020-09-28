package de.ids_mannheim.korap.tokenizer;

import picocli.CommandLine;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.Callable;

@CommandLine.Command(mixinStandardHelpOptions = true,
        name = "koraptokenizer", version = "{}", description = "Tokenizes (and sentence splits) text input.")
public class Main implements Callable<Integer> {

    @CommandLine.Option(names = {"-T",  "--tokenizer-class"}, description = "Class name of the actual tokenizer that will be used (default: ${DEFAULT-VALUE})")
    String tokenizerClassName = DerekoDfaTokenizer_de.class.getName();

    @CommandLine.Option(names = {"--no-tokens"}, negatable = true, description = "Print tokens (default: ${DEFAULT-VALUE})")
    boolean tokens = true;

    @CommandLine.Option(names = {"-p", "--positions"}, description = "Print token start and end positions as character offsets (default: ${DEFAULT-VALUE})")
    boolean positions = false;

    @CommandLine.Option(names = {"-s", "--sentence-boundaries"}, description = "Print sentence boundary positions (default: ${DEFAULT-VALUE})")
    boolean sentencize = false;

    @CommandLine.Option(names = {"-ktt"}, hidden = true, description = "Deprecated. For internal use only. (default: ${DEFAULT-VALUE})")
    boolean ktt = false;

    @CommandLine.Option(names = {"-n", "--normalize"}, description = "Normalize tokens (default: ${DEFAULT-VALUE})")
    boolean normalize = false;

    @SuppressWarnings("CanBeFinal")
    @CommandLine.Option(names = {"-o",
            "--output-file"}, paramLabel = "FILE", description = "Output file (default: ${DEFAULT-VALUE})")
    String output_filename = "-";

    @SuppressWarnings("CanBeFinal")
    @CommandLine.Option(names = {"-e",
            "--encoding"}, description = "Input encoding (default: ${DEFAULT-VALUE})")
    Charset encoding = StandardCharsets.UTF_8;

    @SuppressWarnings("CanBeFinal")
    @CommandLine.Option(names = {"--force"}, description = "Force overwrite (default: ${DEFAULT-VALUE})")
    boolean force_overwrite = false;


    @CommandLine.Parameters(arity = "0..*", paramLabel = "FILES", description = "input files")
    private final ArrayList<String> inputFiles = new ArrayList<>();

    public Main() {

    }

    public static void main(String[] args) {
        new CommandLine(new Main()).execute(args);
    }

    @Override
    public Integer call() throws FileNotFoundException {
        final PrintStream output_stream;
        if ((output_filename == null) || output_filename.equals("-")) {
            output_stream = System.out;
        } else {
            File f = Utils.createFile(output_filename, force_overwrite);
            output_stream = new PrintStream(new BufferedOutputStream(new FileOutputStream(f)));
        }

        for (int i = 0; i < inputFiles.size() || (i == 0 && inputFiles.size() == 0); i++) {
            String fn = (inputFiles.size() > 0 ? inputFiles.get(i) : "-");
            try {
                BufferedReader br = "-".equals(fn) ? new BufferedReader(new InputStreamReader(System.in, encoding)) :
                        Files.newBufferedReader(new File(fn).toPath(), encoding);
                new KorapTokenizer.Builder()
                        .tokenizerClassName(tokenizerClassName)
                        .inputReader(br)
                        .outputStream(output_stream)
                        .printTokens(tokens)
                        .printOffsets(positions)
                        .normalize(normalize)
                        .splitSentences(sentencize)
                        .setEcho(true)
                        .build()
                        .scan();
            } catch (FileNotFoundException e) {
                System.err.println("File not found : \"" + fn + "\"");
            } catch (IOException e) {
                System.err.println("IO error scanning file \"" + fn + "\"");
                System.err.println(e);
            } catch (Exception e) {
                System.err.println("Unexpected exception:");
                e.printStackTrace();
            }
        }
        if ((output_filename != null) && !output_filename.equals("-")) {
            output_stream.close();
        }
        return 0;
    }
}

