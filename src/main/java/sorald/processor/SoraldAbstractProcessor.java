package sorald.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.Constants;
import sorald.FileUtils;
import sorald.UniqueTypesCollector;
import sorald.annotations.ProcessorAnnotation;
import sorald.event.EventHelper;
import sorald.event.EventMetadata;
import sorald.event.EventType;
import sorald.event.SoraldEventHandler;
import sorald.segment.Node;
import sorald.sonar.Checks;
import sorald.sonar.RuleVerifier;
import sorald.sonar.RuleViolation;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtElement;

/** superclass for all processors */
public abstract class SoraldAbstractProcessor<E extends CtElement> extends AbstractProcessor<E> {
    private Set<RuleViolation> ruleViolations;
    private int maxFixes = Integer.MAX_VALUE;
    private int nbFixes = 0;
    private List<SoraldEventHandler> eventHandlers;

    public SoraldAbstractProcessor() {}

    public SoraldAbstractProcessor initResource(String originalFilesPath, File baseDir) {
        JavaFileScanner sonarCheck = Checks.getCheckInstance(getRuleKey());
        try {
            List<String> filesToScan = new ArrayList<>();
            File file = new File(originalFilesPath);
            if (file.isFile()) {
                filesToScan.add(file.getAbsolutePath());
            } else {
                try {
                    filesToScan =
                            FileUtils.findFilesByExtension(file, Constants.JAVA_EXT).stream()
                                    .map(File::getAbsolutePath)
                                    .collect(Collectors.toList());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ruleViolations = RuleVerifier.analyze(filesToScan, baseDir, sonarCheck);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public SoraldAbstractProcessor initResource(List<Node> segment, File baseDir) {
        JavaFileScanner sonarCheck = Checks.getCheckInstance(getRuleKey());
        List<String> filesToScan = new ArrayList<>();
        for (Node node : segment) {
            if (node.isFileNode()) {
                filesToScan.addAll(node.getJavaFiles());
            } else {
                try (Stream<Path> walk = Files.walk(Paths.get(node.getRootPath()))) {
                    filesToScan.addAll(
                            walk.map(x -> x.toFile().getAbsolutePath())
                                    .filter(f -> f.endsWith(Constants.JAVA_EXT))
                                    .collect(Collectors.toList()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        ruleViolations = RuleVerifier.analyze(filesToScan, baseDir, sonarCheck);
        return this;
    }

    public SoraldAbstractProcessor setMaxFixes(int maxFixes) {
        this.maxFixes = maxFixes;
        return this;
    }

    public SoraldAbstractProcessor setNbFixes(int nbFixes) {
        this.nbFixes = nbFixes;
        return this;
    }

    public SoraldAbstractProcessor<?> setEventHandlers(List<SoraldEventHandler> eventHandlers) {
        this.eventHandlers = eventHandlers;
        return this;
    }

    public int getNbFixes() {
        return this.nbFixes;
    }

    public boolean isToBeProcessedAccordingToStandards(CtElement element) {
        return (this.nbFixes < this.maxFixes) && this.isToBeProcessedAccordingToSonar(element);
    }

    public boolean isToBeProcessedAccordingToSonar(CtElement element) {
        if (element == null) {
            return false;
        }
        if (!element.getPosition().isValidPosition()) {
            return false;
        }
        int line = element.getPosition().getLine();
        String file = element.getPosition().getFile().getAbsolutePath();

        try (Stream<String> lines = Files.lines(Paths.get(file))) {
            if (lines.skip(line - 1).findFirst().get().contains("NOSONAR")) {
                return false;
            }
        } catch (IOException e) {
        }

        for (RuleViolation ruleViolation : ruleViolations) {
            if (ruleViolation.getLineNumber() == line
                    && FileUtils.pathAbsNormEqual(ruleViolation.getFileName(), file)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void process(E element) {
        EventHelper.fireEvent(
                eventHandlers,
                EventType.REPAIR,
                new EventMetadata(element.getPosition().toString())
                        .put("key", getRuleKey()).put("position", element.getPosition()));
        UniqueTypesCollector.getInstance().collect(element);
        this.nbFixes++;
    }

    /** @return The numerical identifier of the rule this processor is related to */
    public String getRuleKey() {
        return Arrays.stream(getClass().getAnnotationsByType(ProcessorAnnotation.class))
                .map(ProcessorAnnotation::key)
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        getClass().getName() + " does not have a key"))
                .toString();
    }
}
