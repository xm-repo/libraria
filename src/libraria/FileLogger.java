package libraria;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileLogger {

    private final String mLogFile;

    FileLogger(Path targetDirectory) {

        try {
            Files.createDirectories(targetDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mLogFile = targetDirectory.resolve("log.txt").toString();
    }

    public void log(String message) {

        try(FileWriter fw = new FileWriter(mLogFile, true)) {
            fw.write(message + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
