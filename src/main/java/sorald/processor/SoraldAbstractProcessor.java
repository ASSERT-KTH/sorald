package sorald.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.java.AnalyzerMessage;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.Constants;
import sorald.UniqueTypesCollector;
import sorald.segment.Node;
import sorald.sonar.RuleVerifier;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtElement;

/** superclass for all processors */
public abstract class SoraldAbstractProcessor<E extends CtElement> extends AbstractProcessor<E> {
    private Set<Bug> bugs;
    private int maxFixes = Integer.MAX_VALUE;
    private int nbFixes = 0;

    public SoraldAbstractProcessor() {}

    public abstract JavaFileScanner getSonarCheck();

    public SoraldAbstractProcessor initResource(String originalFilesPath) {
        JavaFileScanner sonarCheck = getSonarCheck();
        try {
            List<String> filesToScan = new ArrayList<>();
            File file = new File(originalFilesPath);
            if (file.isFile()) {
                filesToScan.add(file.getAbsolutePath());
            } else {
                try (Stream<Path> walk = Files.walk(Paths.get(file.getAbsolutePath()))) {
                    filesToScan =
                            walk.map(x -> x.toFile().getAbsolutePath())
                                    .filter(f -> f.endsWith(Constants.JAVA_EXT))
                                    .collect(Collectors.toList());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Set<AnalyzerMessage> issues = RuleVerifier.analyze(filesToScan, sonarCheck);
            bugs = new HashSet<>();
            for (AnalyzerMessage message : issues) {
                Bug BugOffline = new Bug(message);
                bugs.add(BugOffline);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public SoraldAbstractProcessor initResource(List<Node> segment) throws Exception {
        JavaFileScanner sonarCheck = getSonarCheck();
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

        Set<AnalyzerMessage> issues = RuleVerifier.analyze(filesToScan, sonarCheck);
        bugs = new HashSet<>();
        for (AnalyzerMessage message : issues) {
            Bug BugOffline = new Bug(message);
            bugs.add(BugOffline);
        }

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

        for (Bug bug : bugs) {
            if (bug.getLineNumber() == line && bug.getFileName().equals(file)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void process(E element) {
        UniqueTypesCollector.getInstance().collect(element);
        this.nbFixes++;
    }

    class Bug {
        private int lineNumber;
        private String fileName;

        public Bug(AnalyzerMessage message) {
            this.lineNumber = message.getLine();
            this.fileName = message.getInputComponent().key().replace(":", "");
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public String getFileName() {
            return fileName;
        }
    }
}
