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
import org.sonar.java.checks.verifier.MultipleFilesJavaCheckVerifier;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.Constants;
import sorald.UniqueTypesCollector;
import sorald.FileTreeAlgorithm.Node;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtElement;

/** superclass for all processors */
public abstract class SoraldAbstractProcessor<E extends CtElement> extends AbstractProcessor<E> {
	private Set<Bug> bugs;
	private int maxFixes = Integer.MAX_VALUE;
	private int nbFixes = 0;

	SoraldAbstractProcessor(String originalFilesPath) {
		JavaFileScanner sonarCheck = getSonarCheck();
		try {
			List<String> filesToScan = new ArrayList<>();
			File file = new File(originalFilesPath);
			if (file.isFile()) {
				filesToScan.add(file.getAbsolutePath());
			} else {
				try (Stream<Path> walk = Files.walk(Paths.get(file.getAbsolutePath()))) {
					filesToScan = walk.map(x -> x.toFile().getAbsolutePath())
							.filter(f -> f.endsWith(Constants.JAVA_EXT)).collect(Collectors.toList());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Set<AnalyzerMessage> issues = MultipleFilesJavaCheckVerifier.verify(filesToScan, sonarCheck, false);
			bugs = new HashSet<>();
			for (AnalyzerMessage message : issues) {
				Bug BugOffline = new Bug(message);
				bugs.add(BugOffline);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public abstract JavaFileScanner getSonarCheck();

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
