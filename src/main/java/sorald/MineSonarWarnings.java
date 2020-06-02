package sorald;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.stringparsers.FileStringParser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.java.AnalyzerMessage;
import org.sonar.java.checks.ArrayHashCodeAndToStringCheck;
import org.sonar.java.checks.BigDecimalDoubleConstructorCheck;
import org.sonar.java.checks.CastArithmeticOperandCheck;
import org.sonar.java.checks.CompareStringsBoxedTypesWithEqualsCheck;
import org.sonar.java.checks.CompareToReturnValueCheck;
import org.sonar.java.checks.EqualsOnAtomicClassCheck;
import org.sonar.java.checks.GetClassLoaderCheck;
import org.sonar.java.checks.IteratorNextExceptionCheck;
import org.sonar.java.checks.MathOnFloatCheck;
import org.sonar.java.checks.SelfAssignementCheck;
import org.sonar.java.checks.SynchronizationOnStringOrBoxedCheck;
import org.sonar.java.checks.synchronization.SynchronizationOnGetClassCheck;
import org.sonar.java.checks.unused.UnusedThrowableCheck;
import org.sonar.java.checks.verifier.MultipleFilesJavaCheckVerifier;
import org.sonar.java.se.checks.UnclosedResourcesCheck;
import org.sonar.plugins.java.api.JavaFileScanner;

public class MineSonarWarnings {

	private static final List<JavaFileScanner> SONAR_CHECKS = init();

	private static List init() {
		List<JavaFileScanner> TEMP_SONAR_CHECKS = new ArrayList<>();
		TEMP_SONAR_CHECKS.add(new ArrayHashCodeAndToStringCheck());
		TEMP_SONAR_CHECKS.add(new BigDecimalDoubleConstructorCheck());
		TEMP_SONAR_CHECKS.add(new CastArithmeticOperandCheck());
		TEMP_SONAR_CHECKS.add(new CompareStringsBoxedTypesWithEqualsCheck());
		TEMP_SONAR_CHECKS.add(new CompareToReturnValueCheck());
		TEMP_SONAR_CHECKS.add(new EqualsOnAtomicClassCheck());
		TEMP_SONAR_CHECKS.add(new GetClassLoaderCheck());
		TEMP_SONAR_CHECKS.add(new IteratorNextExceptionCheck());
		TEMP_SONAR_CHECKS.add(new MathOnFloatCheck());
		TEMP_SONAR_CHECKS.add(new SelfAssignementCheck());
		TEMP_SONAR_CHECKS.add(new SynchronizationOnGetClassCheck());
		TEMP_SONAR_CHECKS.add(new SynchronizationOnStringOrBoxedCheck());
		TEMP_SONAR_CHECKS.add(new UnclosedResourcesCheck());
		TEMP_SONAR_CHECKS.add(new UnusedThrowableCheck());
		return TEMP_SONAR_CHECKS;
	}

	public static JSAP defineArgs() throws JSAPException {
		JSAP jsap = new JSAP();

		FlaggedOption opt = new FlaggedOption(Constants.ARG_ORIGINAL_FILES_PATH);
		opt.setLongFlag(Constants.ARG_ORIGINAL_FILES_PATH);
		opt.setStringParser(FileStringParser.getParser().setMustExist(true));
		opt.setRequired(true);
		opt.setHelp("The path to the file or folder to be analyzed.");
		jsap.registerParameter(opt);

		Switch sw = new Switch("help");
		sw.setShortFlag('h');
		sw.setLongFlag("help");
		sw.setDefault("false");
		jsap.registerParameter(sw);

		return jsap;
	}

	public static void checkArguments(JSAP jsap, JSAPResult arguments) {
		if (!arguments.success()) {
			for (java.util.Iterator<?> errors = arguments.getErrorMessageIterator(); errors.hasNext();) {
				System.err.println("Error: " + errors.next());
			}
			printUsage(jsap);
		}

		if (arguments.getBoolean("help")) {
			printUsage(jsap);
		}
	}

	public static void printUsage(JSAP jsap) {
		System.err.println("Arguments: ");
		System.err.println();
		System.err.println(jsap.getHelp());
		System.exit(-1);
	}

	public static void main(String[] args) throws JSAPException {
		JSAP jsap = defineArgs();
		JSAPResult arguments = jsap.parse(args);
		checkArguments(jsap, arguments);
		String projectPath = arguments.getFile(Constants.ARG_ORIGINAL_FILES_PATH).getAbsolutePath();

		Map<String, Integer> warnings = new HashMap<>();
		try {
			List<String> filesToScan = new ArrayList<>();
			File file = new File(projectPath);
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
			for (JavaFileScanner javaFileScanner : SONAR_CHECKS) {
				Set<AnalyzerMessage> issues = MultipleFilesJavaCheckVerifier.verify(filesToScan, javaFileScanner, false);
				warnings.putIfAbsent(javaFileScanner.getClass().getSimpleName(), issues.size());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		warnings.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.forEach(System.out::println);
	}

}
