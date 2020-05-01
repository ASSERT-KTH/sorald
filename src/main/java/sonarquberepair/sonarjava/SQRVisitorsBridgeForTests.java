package sonarquberepair.sonarjava;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.java.AnalyzerMessage;
import org.sonar.java.SonarComponents;
import org.sonar.java.resolve.SemanticModel;
import org.sonar.java.se.SymbolicExecutionMode;
import org.sonar.plugins.java.api.JavaCheck;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.JavaVersion;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.Tree;

import org.sonar.java.model.VisitorsBridge;
import org.sonar.java.model.DefaultJavaFileScannerContext;

/* Taken from https://github.com/SonarSource/sonar-java/blob/5.13.1.18282/java-frontend/src/main/java/org/sonar/java/model/VisitorsBridgeForTests.java */
public class SQRVisitorsBridgeForTests extends VisitorsBridge {

  private TestJavaFileScannerContext testContext;
  private boolean enableSemantic = true;

  @VisibleForTesting
  public SQRVisitorsBridgeForTests(JavaFileScanner visitor, SonarComponents sonarComponents) {
    this(Collections.singletonList(visitor), new ArrayList<>(), sonarComponents);
  }

  public SQRVisitorsBridgeForTests(Iterable visitors, @Nullable SonarComponents sonarComponents) {
    super(visitors, new ArrayList<>(), sonarComponents, SymbolicExecutionMode.DISABLED);
    enableSemantic = false;
  }

  public SQRVisitorsBridgeForTests(Iterable visitors, List<File> projectClasspath, @Nullable SonarComponents sonarComponents) {
    super(visitors, projectClasspath, sonarComponents, SymbolicExecutionMode.getMode(Iterables.<JavaCheck>toArray(visitors, JavaCheck.class), true));
  }

  @Override
  protected JavaFileScannerContext createScannerContext(CompilationUnitTree tree, SemanticModel semanticModel,
                                                        SonarComponents sonarComponents, boolean failedParsing) {
    SemanticModel model = enableSemantic ? semanticModel : null;
    TestJavaFileScannerContext context = new TestJavaFileScannerContext(tree, currentFile, model, sonarComponents, javaVersion, failedParsing);
    if (testContext != null) {
      context.issues.addAll(this.testContext.issues);
    }
    testContext = context;
    return testContext;
  }

  public TestJavaFileScannerContext lastCreatedTestContext() {
    return testContext;
  }

  public static class TestJavaFileScannerContext extends DefaultJavaFileScannerContext {

    private final Set<AnalyzerMessage> issues = new HashSet<>();
    private final SonarComponents sonarComponents;

    public TestJavaFileScannerContext(CompilationUnitTree tree, InputFile inputFile, SemanticModel semanticModel,
                                      @Nullable SonarComponents sonarComponents, JavaVersion javaVersion, boolean failedParsing) {
      super(tree, inputFile, semanticModel, sonarComponents, javaVersion, failedParsing);
      this.sonarComponents = sonarComponents;
    }

    public Set<AnalyzerMessage> getIssues() {
      return issues;
    }

    /**
     * @deprecated since SonarJava 5.12 - Should only report on InputComponent
     */
    @Deprecated
    @Override
    public void addIssue(File file, JavaCheck javaCheck, int line, String message) {
      issues.add(new AnalyzerMessage(javaCheck, sonarComponents.inputFromIOFileOrDirectory(file), line, message, 0));
    }

    @Override
    public void addIssueOnProject(JavaCheck javaCheck, String message) {
      issues.add(new AnalyzerMessage(javaCheck, sonarComponents.project(), null, message, 0));
    }

    @Override
    public void addIssue(int line, JavaCheck javaCheck, String message, @Nullable Integer cost) {
      issues.add(new AnalyzerMessage(javaCheck, getInputFile(), line, message, cost != null ? cost.intValue() : 0));
    }

    @Override
    public void reportIssue(JavaCheck javaCheck, Tree syntaxNode, String message, List<JavaFileScannerContext.Location> secondary, @Nullable Integer cost) {
      List<List<JavaFileScannerContext.Location>> flows = secondary.stream().map(Collections::singletonList).collect(Collectors.toList());
      issues.add(createAnalyzerMessage(getInputFile(), javaCheck, syntaxNode, null, message, flows, cost));
    }

    @Override
    public void reportIssue(JavaCheck javaCheck, Tree startTree, Tree endTree, String message) {
      issues.add(createAnalyzerMessage(javaCheck, startTree, endTree, message, Collections.emptyList(), null));
    }

    @Override
    public void reportIssue(JavaCheck javaCheck, Tree startTree, Tree endTree, String message, List<JavaFileScannerContext.Location> secondary, @Nullable Integer cost) {
      issues.add(createAnalyzerMessage(javaCheck, startTree, endTree, message, secondary, cost));
    }

    @Override
    public void reportIssueWithFlow(JavaCheck javaCheck, Tree syntaxNode, String message, Iterable<List<JavaFileScannerContext.Location>> flows, @Nullable Integer cost) {
      issues.add(createAnalyzerMessage(getInputFile(), javaCheck, syntaxNode, null, message, flows, cost));
    }

    @Override
    public void reportIssue(AnalyzerMessage message) {
      issues.add(message);
    }

    @Override
    public AnalyzerMessage createAnalyzerMessage(JavaCheck javaCheck, Tree startTree, String message) {
      return createAnalyzerMessage(getInputFile(), javaCheck, startTree, null, message, Arrays.asList(), null);
    }

    private AnalyzerMessage createAnalyzerMessage(JavaCheck javaCheck, Tree startTree, @Nullable Tree endTree, String message, List<JavaFileScannerContext.Location> secondary, @Nullable Integer cost) {
      List<List<JavaFileScannerContext.Location>> flows = secondary.stream().map(Collections::singletonList).collect(Collectors.toList());  
      return createAnalyzerMessage(getInputFile(), javaCheck, startTree, endTree, message, flows, cost);
    }
  }
}
