package sonarquberepair.sonarjava;

import com.google.common.annotations.Beta;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.java.AnalyzerMessage;
import org.sonar.java.ast.JavaAstScanner;
import org.sonar.java.ast.parser.JavaParser;
import org.sonar.java.model.JavaVersionImpl;
import org.sonar.plugins.java.api.JavaFileScanner;


/* Taken from https://github.com/SonarSource/sonar-java/blob/5.13.1.18282/java-checks-testkit/src/main/java/org/sonar/java/checks/verifier/MultipleFilesJavaCheckVerifier.java */
public class SQRMultipleFilesJavaCheckVerifier extends CheckVerifier {

  /**
   * Verifies that all the expected issues are raised after analyzing all the given files with the given check.
   *
   * <br /><br />
   *
   * By default, any jar or zip archive present in the folder defined by {@link JavaCheckVerifier#DEFAULT_TEST_JARS_DIRECTORY} will be used
   * to add extra classes to the classpath. If this folder is empty or does not exist, then the analysis will be based on the source of
   * the provided file.
   *
   * @param filesToScan The files to be analyzed
   * @param check The check to be used for the analysis
   */
  public static Set<AnalyzerMessage> verify(List<String> filesToScan, JavaFileScanner check, boolean test) {
    return verify(new SQRMultipleFilesJavaCheckVerifier(), filesToScan, check, false, true, test);
  }

  /**
   * Verifies that no issues are raised after analyzing all the given files with the given check.
   *
   * @param filesToScan The files to be analyzed
   * @param check The check to be used for the analysis
   */
  public static void verifyNoIssue(List<String> filesToScan, JavaFileScanner check) {
    verify(new SQRMultipleFilesJavaCheckVerifier(), filesToScan, check, true, true, true);
  }

  /**
   * Verifies that no issues are raised after analyzing all given files with the given check when semantic is not available.
   *
   * @param filesToScan The files to be analyzed
   * @param check The check to be used for the analysis
   */
  public static void verifyNoIssueWithoutSemantic(List<String> filesToScan, JavaFileScanner check) {
    SQRMultipleFilesJavaCheckVerifier verifier = new SQRMultipleFilesJavaCheckVerifier() {
      @Override
      public String getExpectedIssueTrigger() {
        return "// NOSEMANTIC_ISSUE";
      }
    };
    verify(verifier, filesToScan, check, true, false, true);
  }

  private static Set<AnalyzerMessage> verify(SQRMultipleFilesJavaCheckVerifier verifier, List<String> filesToScan, JavaFileScanner check, boolean expectNoIssues, boolean withSemantic, boolean test) {
    if (expectNoIssues) {
      verifier.expectNoIssues();
    }
    Set<AnalyzerMessage> issues = verifier.scanFiles(filesToScan, check, withSemantic);
    if (test) {
      verifier.checkIssues(issues, expectNoIssues);
      return null;
    } else {
      return issues;
    }
  }

  private Set<AnalyzerMessage> scanFiles(List<String> filesToScan, JavaFileScanner check, boolean withSemantic) {
    List<File> classPath = JavaCheckVerifier.getClassPath(JavaCheckVerifier.DEFAULT_TEST_JARS_DIRECTORY);
    SQRVisitorsBridgeForTests visitorsBridge;
    if (withSemantic) {
      visitorsBridge = new SQRVisitorsBridgeForTests(Arrays.asList(check, new JavaCheckVerifier.ExpectedIssueCollector(this)), classPath, null);
    } else {
      visitorsBridge = new SQRVisitorsBridgeForTests(Arrays.asList(check, new JavaCheckVerifier.ExpectedIssueCollector(this)), null);
    }
    visitorsBridge.setJavaVersion(new JavaVersionImpl());
    JavaAstScanner astScanner = new JavaAstScanner(JavaParser.createParser(), null);
    astScanner.setVisitorBridge(visitorsBridge);
    astScanner.scan(filesToScan.stream()
      .map(filename -> new TestInputFileBuilder("", filename).setCharset(StandardCharsets.UTF_8).build())
      .collect(Collectors.toList()));

    SQRVisitorsBridgeForTests.TestJavaFileScannerContext testJavaFileScannerContext = visitorsBridge.lastCreatedTestContext();
    return testJavaFileScannerContext.getIssues();
  }

  @Override
  public String getExpectedIssueTrigger() {
    return "// " + ISSUE_MARKER;
  }

}