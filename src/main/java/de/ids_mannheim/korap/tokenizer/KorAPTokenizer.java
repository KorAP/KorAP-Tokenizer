package de.ids_mannheim.korap.tokenizer;

import picocli.CommandLine;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.Callable;

@CommandLine.Command(mixinStandardHelpOptions = true,
        name = "koraptokenizer", version = "{}", description = "Tokenizes (and sentence splits) text input.")
public class KorAPTokenizer implements Callable<Integer> {

    @CommandLine.Option(names = {"--no-tokens"}, negatable = true, description = "Print tokens (default: ${DEFAULT-VALUE})")
    boolean tokens = true;

    @CommandLine.Option(names = {"-p", "--positions"}, description = "Print token start and end positions as character offsets (default: ${DEFAULT-VALUE})")
    boolean positions = false;

    @CommandLine.Option(names = {"-s", "--sentence-boundaries"}, description = "Print sentence boundary positions (default: ${DEFAULT-VALUE})")
    boolean sentencize = false;

    @CommandLine.Option(names = {"-ktt"}, description = "Deprecated. For internal use only. (default: ${DEFAULT-VALUE})")
    boolean ktt = false;

    @CommandLine.Option(names = {"-n", "--normalize"}, description = "Normalize tokens (default: ${DEFAULT-VALUE})")
    boolean normalize = false;

    @SuppressWarnings("CanBeFinal")
    @CommandLine.Option(names = {"-o",
            "--output-file"}, description = "Output file (default: ${DEFAULT-VALUE})")
    String output_fillename = "-";

    @SuppressWarnings("CanBeFinal")
    @CommandLine.Option(names = {"--force"}, description = "Force overwrite (default: ${DEFAULT-VALUE})")
    boolean force_overwrite = false;


    @CommandLine.Parameters(arity = "0..*", description = "input files")
    private final ArrayList<String> inputFiles = new ArrayList<>();

    public KorAPTokenizer() {

    }

    public static void main(String[] args) {
        new CommandLine(new KorAPTokenizer()).execute(args);
    }

    @Override
    public Integer call() throws FileNotFoundException {
        final PrintStream output_stream;
        if ((output_fillename == null) || output_fillename.equals("-")) {
            output_stream = System.out;
        } else {
            File f = Utils.createFile(output_fillename, force_overwrite);
            output_stream = new PrintStream(new BufferedOutputStream(new FileOutputStream(f)));
        }

        for (int i = 0; i < inputFiles.size() || (i == 0 && inputFiles.size() == 0); i++) {
            KorAPTokenizerImpl scanner = null;
            String fn = (inputFiles.size() > 0 ? inputFiles.get(i) : "-");
            try {
                BufferedReader br = "-".equals(fn) ? new BufferedReader(new InputStreamReader(System.in)) :
                        new BufferedReader(new FileReader(fn));
                scanner = new KorAPTokenizerImpl(br, output_stream, true, tokens, sentencize, positions,  ktt, normalize);
                scanner.scanThrough();
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
        if ((output_fillename != null) && !output_fillename.equals("-")) {
            output_stream.close();
        }
        return 0;
    }
}

