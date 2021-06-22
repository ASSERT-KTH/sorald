package sorald.sonar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sonar.java.checks.DefaultPackageCheck;
import sorald.TestHelper;
import sorald.processor.ArrayHashCodeAndToStringProcessor;
import sorald.processor.MathOnFloatProcessor;

class RuleVerifierTest {

    @Test
    public void analyze_filtersOutMessages_thatCorrespondToMethodSuppressedWarning()
            throws IllegalAccessException, InstantiationException {
        String suppressedRuleKey = new ArrayHashCodeAndToStringProcessor().getRuleKey();
        String nonSuppressedRuleKey = new MathOnFloatProcessor().getRuleKey();
        String testFile =
                TestHelper.PATH_TO_RESOURCES_FOLDER
                        .resolve("WithMethodSuppressed" + suppressedRuleKey + ".java")
                        .toString();

        var violations =
                RuleVerifier.analyze(
                        List.of(testFile),
                        TestHelper.PATH_TO_RESOURCES_FOLDER.toFile(),
                        List.of(
                                Checks.getCheckInstance(suppressedRuleKey),
                                Checks.getCheckInstance(nonSuppressedRuleKey)));

        assertThat(violations, is(not(empty())));
        Set<String> violatedRuleKeys =
                violations.stream().map(RuleViolation::getRuleKey).collect(Collectors.toSet());
        assertThat(nonSuppressedRuleKey, in(violatedRuleKeys));
        assertThat(suppressedRuleKey, not(in(violatedRuleKeys)));
    }

    @Test
    public void analyze_filtersOutMessages_thatLackPrimaryLocation() {
        var checkWithNoLocation = new DefaultPackageCheck();

        String testFile =
                TestHelper.PATH_TO_RESOURCES_FOLDER
                        .resolve("ArrayHashCodeAndToString.java")
                        .toString();
        var violations =
                RuleVerifier.analyze(
                        List.of(testFile),
                        TestHelper.PATH_TO_RESOURCES_FOLDER.toFile(),
                        checkWithNoLocation);

        assertThat(violations, is(empty()));
    }

    @Test
    public void analyze_doesNotReturnViolation_fromNosonarLine() throws IOException {
        // arrange
        Path testFile = TestHelper.PATH_TO_RESOURCES_FOLDER.resolve("NOSONARCommentTest.java");
        int nosonarLine = 7;
        int violationLine = 8;
        List<String> lines = Files.readAllLines(testFile);
        assertThat(lines.get(nosonarLine - 1), containsString("// Noncompliant, NOSONAR"));
        assertThat(lines.get(violationLine - 1), containsString("// Noncompliant"));

        // act
        var violations =
                RuleVerifier.analyze(
                        List.of(testFile.toString()),
                        TestHelper.PATH_TO_RESOURCES_FOLDER.toFile(),
                        List.of(Checks.getCheckInstance("S2116")));

        // assert
        assertThat(violations.size(), equalTo(1));
        assertThat(violations.stream().findFirst().get().getStartLine(), equalTo(violationLine));
    }
}
