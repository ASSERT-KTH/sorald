package sorald.processor;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import org.sonar.plugins.java.api.JavaFileScanner;
import sorald.Constants;
import sorald.Main;
import sorald.TestHelper;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

public class ProcessorTest {

	@ParameterizedTest
	@ArgumentsSource(NonCompliantJavaFileProvider.class)
	public void testProcessSingleFile(ProcessorTestHelper.ProcessorTestCase<? extends JavaFileScanner> testCase) throws Exception {
		String pathToRepairedFile = Paths.get(Constants.SORALD_WORKSPACE)
				.resolve(Constants.SPOONED)
				.resolve(testCase.outfileRelpath)
				.toString();
		String originalFileAbspath = testCase.nonCompliantFile.toPath().toAbsolutePath().toString();

		JavaCheckVerifier.verify(originalFileAbspath, testCase.createCheckInstance());
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
	private static class NonCompliantJavaFileProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
			return Arrays.stream(ProcessorTestHelper.TEST_FILES_ROOT.toFile().listFiles())
					.filter(File::isDirectory)
					.flatMap(dir -> Arrays.stream(dir.listFiles())
							.filter(file -> file.getName().endsWith(".java"))
							.map(ProcessorTestHelper::toProcessorTestCase)
					).map(Arguments::of);
		}
	}
}
