package de.ids_mannheim.korap.tokenizer;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;

public class Utils {
    /**
     * Create a file.
     *
     * @param fname           file name
     * @param force_overwrite force overwrite flag
     * @return the file
     */
    public static File createFile(String fname, boolean force_overwrite) {
        File f = new File(fname);
        try {
            Files.createFile(f.toPath());
        } catch (AccessDeniedException e) {
            final String message = "ERROR: Cannot write file '" + fname + "'";
            System.err.println(message);
            System.exit(-1);
        } catch (FileAlreadyExistsException e) {
            if (!force_overwrite) {
                final String message = "ERROR: '" + fname + "' already exits. Use --force to overwrite";
                System.err.println(message);
                System.exit(-1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }
}
