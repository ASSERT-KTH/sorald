package sorald.processor;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.sonar.check.Rule;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.Constants;
import sorald.Main;
import sorald.TestHelper;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

public class ProcessorTest {

	private static final Path TEST_FILES_ROOT = Paths.get(Constants.PATH_TO_RESOURCES_FOLDER).resolve("processor_test_files");

	@ParameterizedTest
	@ArgumentsSource(NonCompliantJavaFileProvider.class)
	void testProcessSingleFile(ProcessorTestCase testCase) throws Exception {
		String pathToRepairedFile = Paths.get(Constants.SORALD_WORKSPACE)
				.resolve(Constants.SPOONED)
				.resolve(testCase.nonCompliantFile.getName())
				.toString();
		String originalFileAbspath = testCase.nonCompliantFile.toPath().toAbsolutePath().toString();

		JavaCheckVerifier.verify(originalFileAbspath, testCase.checkClass.getConstructor().newInstance());
		Main.main(new String[]{
				Constants.ARG_SYMBOL + Constants.ARG_ORIGINAL_FILES_PATH, originalFileAbspath,
				Constants.ARG_SYMBOL + Constants.ARG_RULE_KEYS, testCase.ruleKey,
				Constants.ARG_SYMBOL + Constants.ARG_WORKSPACE, Constants.SORALD_WORKSPACE});

		TestHelper.removeComplianceComments(pathToRepairedFile);
		JavaCheckVerifier.verifyNoIssue(pathToRepairedFile, testCase.createCheckInstance());
	}

	/**
	 * Provider class that provides test cases based on the buggy/non-compliant Java source files in the test
	 * files directory.
	 */
	public static class NonCompliantJavaFileProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
			return Arrays.stream(TEST_FILES_ROOT.toFile().listFiles())
					.filter(File::isDirectory)
					.flatMap(dir -> Arrays.stream(dir.listFiles())
							.filter(file -> file.getName().endsWith(".java"))
							.map(ProcessorTest::toTestCase)
					).map(Arguments::of);
		}
	}

	/**
	 *  Create a {@link ProcessorTestCase} from a non-compliant Java source file.
	 *
	 *  For this to work out, the directory that the Java file file is located in must carry the same name as a sonar
	 *  check class, minus the "Check" suffix. For example, if the test file is for the rule related to
	 *  {@link org.sonar.java.checks.MathOnFloatCheck}, the directory must be called "MathOnFloat". The test
	 *  file itself can be called anything. Here's an example of a compliant directory structure, where the Java
	 *  files are test files for {@link org.sonar.java.checks.MathOnFloatCheck}.
	 *
	 *      MathOnFloat
	 *             |
	 *             ---- TestCaseFile.java
	 *             |
	 *             ---- OtherTestCaseFile.java
	 */
	private static ProcessorTestCase toTestCase(File nonCompliantFile) {
		File directory = nonCompliantFile.getParentFile();
		assert directory.isDirectory();
		String ruleName = directory.getName();
		Class<JavaFileScanner> checkClass = loadCheckClass(ruleName);
		return new ProcessorTestCase(ruleName, getRuleKey(checkClass), nonCompliantFile, checkClass);
	}

	private static Class<JavaFileScanner> loadCheckClass(String ruleName) {
		// FIXME This is a ridiculously insecure way to load the class. Should probably use a lookup table instead.
		String checkQualname = "org.sonar.java.checks." + ruleName + "Check";
		try {
			return (Class<JavaFileScanner>) Class.forName(checkQualname);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(checkQualname + " is not a valid class");
		}
	}

	/**
	 * Retrieve the numeric identifier of the rule related to the given check class. Non-digits are stripped, so
	 * e.g. S1234 becomes 1234.
	 */
	private static String getRuleKey(Class<JavaFileScanner> checkClass) {
		return Arrays.stream(checkClass.getAnnotationsByType(Rule.class))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(checkClass.getName() + " does not have a key"))
				.key()
				.replaceAll("[^\\d]+", "");
	}

	/**
	 * A wrapper class to hold the information required to execute a test case for a single file and rule with the
	 * associated processor.
	 */
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
					" source=" + TEST_FILES_ROOT.relativize(nonCompliantFile.toPath());
		}

		JavaFileScanner createCheckInstance() throws
				NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
			return checkClass.getConstructor().newInstance();
		}
	}
}
