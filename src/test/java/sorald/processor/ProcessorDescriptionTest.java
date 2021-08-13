package sorald.processor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import sorald.Processors;

public class ProcessorDescriptionTest {
    private static final Path PROCESSOR_PACKAGE = Paths.get("src/main/java/sorald/processor");

    @Test
    public void test_eachProcessorIsAccompaniedByDescription() {
        List<File> processors =
                Processors.getAllProcessors().stream()
                        .map(Class::getSimpleName)
                        .map(procName -> PROCESSOR_PACKAGE.resolve(procName + ".java"))
                        .map(Path::toFile)
                        .collect(Collectors.toList());
        processors.forEach(
                processor ->
                        assertTrue(
                                getDescription(processor).isFile(),
                                "Description corresponding to "
                                        + processor.getName()
                                        + " does not exist."));
    }

    private static File getDescription(File processor) {
        String processorName = FilenameUtils.getBaseName(processor.getName());
        String descriptionFileName = processorName + ".md";
        return Paths.get(PROCESSOR_PACKAGE + "/" + descriptionFileName).toFile();
    }
}
