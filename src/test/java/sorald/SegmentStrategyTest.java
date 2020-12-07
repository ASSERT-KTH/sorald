package sorald;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.java.checks.ArrayHashCodeAndToStringCheck;
import sorald.processor.ArrayHashCodeAndToStringProcessor;
import sorald.processor.ProcessorTestHelper;
import sorald.processor.SoraldAbstractProcessor;
import sorald.segment.Node;
import sorald.sonar.Checks;
import sorald.sonar.ProjectScanner;
import sorald.sonar.RuleVerifier;
import sorald.sonar.RuleViolation;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtType;

public class SegmentStrategyTest {
    @Test
    public void arrayToStringProcessor_success_Test() throws Exception {
        String fileName = "ArrayHashCodeAndToString.java";
        String pathToBuggyFile = Constants.PATH_TO_RESOURCES_FOLDER + fileName;
        String pathToRepairedFile =
                Constants.SORALD_WORKSPACE + "/SEGMENT/" + Constants.SPOONED + "/" + fileName;

        RuleVerifier.verifyHasIssue(pathToBuggyFile, new ArrayHashCodeAndToStringCheck());
        Main.main(
                new String[] {
                    Constants.REPAIR_COMMAND_NAME,
                    Constants.ARG_REPAIR_STRATEGY,
                    "SEGMENT",
                    // FIXME MAX_FILES_PER_SEGMENT is set to 1 as a temporary fix to
                    // https://github.com/SpoonLabs/sorald/issues/154
                    Constants.ARG_MAX_FILES_PER_SEGMENT,
                    "1",
                    Constants.ARG_ORIGINAL_FILES_PATH,
                    Constants.PATH_TO_RESOURCES_FOLDER,
                    Constants.ARG_RULE_KEYS,
                    "2116",
                    Constants.ARG_PRETTY_PRINTING_STRATEGY,
                    PrettyPrintingStrategy.NORMAL.name(),
                    Constants.ARG_WORKSPACE,
                    Constants.SORALD_WORKSPACE + "/SEGMENT/"
                });
        TestHelper.removeComplianceComments(pathToRepairedFile);
        RuleVerifier.verifyNoIssue(pathToRepairedFile, new ArrayHashCodeAndToStringCheck());
    }

    @Test
    public void arrayToStringProcessor_fail_Test() throws Exception {
        String fileName = "ArrayHashCodeAndToString.java";
        String pathToBuggyFile = Constants.PATH_TO_RESOURCES_FOLDER + fileName;

        RuleVerifier.verifyHasIssue(pathToBuggyFile, new ArrayHashCodeAndToStringCheck());
        String[] args =
                new String[] {
                    Constants.REPAIR_COMMAND_NAME,
                    Constants.ARG_REPAIR_STRATEGY,
                    "SEGMENT",
                    Constants.ARG_MAX_FILES_PER_SEGMENT,
                    "0",
                    Constants.ARG_ORIGINAL_FILES_PATH,
                    Constants.PATH_TO_RESOURCES_FOLDER,
                    Constants.ARG_RULE_KEYS,
                    "2116",
                    Constants.ARG_PRETTY_PRINTING_STRATEGY,
                    PrettyPrintingStrategy.NORMAL.name(),
                    Constants.ARG_WORKSPACE,
                    Constants.SORALD_WORKSPACE + "/SEGMENT/"
                };
        assertThrows(SystemExitHandler.NonZeroExit.class, () -> Main.main(args));
    }

    @Test
    public void segmentStrategy_doesNotFail_onCrashInParsingSegment(@TempDir File tempDir)
            throws IOException {
        // arrange
        org.apache.commons.io.FileUtils.copyDirectory(
                new File(Constants.PATH_TO_RESOURCES_FOLDER), tempDir);

        SoraldConfig config = createSegmentConfig(tempDir.getAbsolutePath());

        SoraldAbstractProcessor<?> processor =
                new ArrayHashCodeAndToStringProcessor().setEventHandlers(List.of());
        Set<RuleViolation> violations =
                ProjectScanner.scanProject(
                        tempDir, tempDir, Checks.getCheckInstance(processor.getRuleKey()));

        // we decide that parsing this class causes crashes
        String crashingClass = "DeadStores";
        Path crashingFile = getProcessorTestJavaFilePath(tempDir, crashingClass);

        Repair repair = new Repair(config, List.of());
        Function<LinkedList<Node>, CtModel> selectivelyCrashySegmentParser =
                segment ->
                        segmentContainsFile(segment, crashingFile.toString())
                                ? throwIllegalStateException()
                                : repair.createSegmentLauncher(segment).getModel();

        // act
        List<CtModel> models =
                repair.segmentRepair(
                                tempDir.getAbsoluteFile().toPath(),
                                processor,
                                violations,
                                selectivelyCrashySegmentParser)
                        .collect(Collectors.toList());

        // assert
        assertThat(processor.getNbFixes(), greaterThan(1));
        assertThat(models.size(), greaterThan(1));
        assertFalse(
                models.stream()
                        .map(CtModel::getAllTypes)
                        .flatMap(Collection::stream)
                        .map(CtType::getSimpleName)
                        .anyMatch(typeName -> typeName.equals(crashingClass)));
    }

    /**
     * @return the absolute path to a Java file in the given directory with the given class name.
     */
    private static Path getProcessorTestJavaFilePath(File root, String className) {
        return ProcessorTestHelper.getTestCaseStream(
                        root.toPath().resolve("processor_test_files").toFile())
                .map(tc -> tc.nonCompliantFile)
                .map(File::toPath)
                .map(Path::toAbsolutePath)
                .filter(p -> p.endsWith(className + Constants.JAVA_EXT))
                .findFirst()
                .get();
    }

    private static SoraldConfig createSegmentConfig(String originalFilesPath) {
        var config = new SoraldConfig();
        config.setRepairStrategy(RepairStrategy.SEGMENT);
        config.setOriginalFilesPath(originalFilesPath);
        config.setWorkspace(Constants.SORALD_WORKSPACE);
        config.setFileOutputStrategy(FileOutputStrategy.CHANGED_ONLY);
        config.setMaxFixesPerRule(Integer.MAX_VALUE);
        config.setMaxFilesPerSegment(1);
        return config;
    }

    private static CtModel throwIllegalStateException() {
        throw new IllegalStateException("Just crashing a little bit here :)");
    }

    private static boolean segmentContainsFile(LinkedList<Node> segment, String fileName) {
        return segment.stream()
                .map(Node::getJavaFiles)
                .flatMap(List::stream)
                .anyMatch(s -> s.endsWith(fileName));
    }
}
