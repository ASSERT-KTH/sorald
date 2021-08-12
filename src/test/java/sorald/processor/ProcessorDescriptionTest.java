package sorald.processor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;

public class ProcessorDescriptionTest {
    private static final Path PROCESSOR_PACKAGE = Paths.get("src/main/java/sorald/processor");

    @Test
    public void test_eachProcessorIsAccompaniedByDescription() {
        List<File> processors =
                Arrays.stream(PROCESSOR_PACKAGE.toFile().listFiles())
                        .filter(file -> file.getName().endsWith(".java"))
                        .collect(Collectors.toList());
        processors.forEach(
                processor -> {
                    if (processor.getName().equals("SoraldAbstractProcessor.java")) return;

                    assertTrue(
                            getDescription(processor).isFile(),
                            "Description corresponding to "
                                    + processor.getName()
                                    + " does not exist.");
                });
    }

    private static File getDescription(File processor) {
        String processorName = FilenameUtils.getBaseName(processor.getName());
        String descriptionFileName = processorName + ".md";
        return Paths.get(PROCESSOR_PACKAGE + "/" + descriptionFileName).toFile();
    }
}
