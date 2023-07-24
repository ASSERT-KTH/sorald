package sorald.processor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sorald.Processors;

public class ProcessorDescriptionTest {
    private static final Path PROCESSOR_PACKAGE = Paths.get("src/main/java/sorald/processor");

    @ParameterizedTest
    @MethodSource("processorFileProvider")
    public void test_eachProcessorIsAccompaniedByDescription(File processor) {
        assertTrue(
                getDescription(processor).isFile(),
                "Description corresponding to " + processor.getName() + " does not exist.");
    }

    private static Stream<Arguments> processorFileProvider() {
        return Processors.getAllProcessors().stream()
                .map(Class::getSimpleName)
                .map(procName -> PROCESSOR_PACKAGE.resolve(procName + ".java"))
                .map(Path::toFile)
                .map(Arguments::of);
    }

    private static File getDescription(File processor) {
        String processorName = FilenameUtils.getBaseName(processor.getName());
        String descriptionFileName = processorName + ".md";
        return Paths.get(PROCESSOR_PACKAGE + "/" + descriptionFileName).toFile();
    }
}
