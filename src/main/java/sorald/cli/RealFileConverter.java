package sorald.cli;

import java.io.File;
import java.nio.file.Paths;
import picocli.CommandLine;

/** Converter that converts a String to a real file that's validated to exist in the file system. */
public class RealFileConverter implements CommandLine.ITypeConverter<File> {
    @Override
    public File convert(String s) throws Exception {
        return Paths.get(s).normalize().toRealPath().toFile();
    }
}
