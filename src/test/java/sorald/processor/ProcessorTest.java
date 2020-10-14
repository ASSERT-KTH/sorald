package sorald.processor;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.sonar.check.Rule;
import org.sonar.java.checks.MathOnFloatCheck;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.Constants;
import sorald.Main;
import sorald.TestHelper;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

public class ProcessorTest {

	private static final Path PATH_TO_TEST_FILES = Paths.get(Constants.PATH_TO_RESOURCES_FOLDER).resolve("processor_test_files");

	@ParameterizedTest
	@ArgumentsSource(NonCompliantJavaFileProvider.class)
	void testParameterized(ProcessorTestCase testCase) throws Exception {
		// filename should be on the form RuleName_Key.java
		String ruleName = testCase.ruleName;
		String ruleKey = testCase.ruleKey;
		Class<JavaFileScanner> checkClass = testCase.checkClass;

		System.out.println(MathOnFloatCheck.class.getAnnotations()[0] instanceof org.sonar.check.Rule);

		String pathToRepairedFile = Constants.SORALD_WORKSPACE + "/" + Constants.SPOONED + "/" + ruleName + ".java";
		String originalFileAbspath = testCase.nonCompliantFile.toPath().toAbsolutePath().toString();

		JavaCheckVerifier.verify(originalFileAbspath, checkClass.getConstructor().newInstance());
		Main.main(new String[]{
				Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH, originalFileAbspath,
				Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS, ruleKey,
				Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE, Constants.SORALD_WORKSPACE});

		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, checkClass.getConstructor().newInstance());
	}

	public static class NonCompliantJavaFileProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
			return Arrays.stream(PATH_TO_TEST_FILES.toFile().listFiles())
					.filter(File::isDirectory)
					.flatMap(dir -> Arrays.stream(dir.listFiles())
							.filter(file -> file.getName().endsWith(".java"))
							.map(ProcessorTest::toTestCase)
					).map(Arguments::of);
		}
	}

	@SuppressWarnings("unchecked")
	private static ProcessorTestCase toTestCase(File nonCompliantFile) {
		File directory = nonCompliantFile.getParentFile();
		assert directory.isDirectory();

		String ruleName = directory.getName();
		String checkQualname = "org.sonar.java.checks." + ruleName + "Check";
		Class<JavaFileScanner> checkClass;
		try {
			checkClass = (Class<JavaFileScanner>) Class.forName(checkQualname);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(checkQualname + " is not a valid class");
		}

		return new ProcessorTestCase(ruleName, getRuleKey(checkClass), nonCompliantFile, checkClass);
	}

	private static String getRuleKey(Class<JavaFileScanner> checkClass) {
		return Arrays.stream(checkClass.getAnnotationsByType(Rule.class))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(checkClass.getName() + " does not have a key"))
				.key()
				.replaceAll("[^\\d]+", "");
	}

	private static class ProcessorTestCase {
		final String ruleName;
		final String ruleKey;
		final File nonCompliantFile;
		final Class<JavaFileScanner> checkClass;

		ProcessorTestCase(String ruleName, String ruleKey, File nonCompliantFile, Class<JavaFileScanner> checkClass) {
			this.ruleName = ruleName;
			this.ruleKey = ruleKey;
			this.nonCompliantFile = nonCompliantFile;
			this.checkClass = checkClass;
		}

		@Override
		public String toString() {
			return "ruleKey=" + ruleKey +
					" ruleName=" + ruleName +
					" source=" + PATH_TO_TEST_FILES.relativize(nonCompliantFile.toPath());
		}
	}
}
