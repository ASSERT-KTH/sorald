package sorald.sonar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sonar.java.checks.DefaultPackageCheck;
import sorald.Constants;

class RuleVerifierTest {

    @Test
    public void analyze_filtersOutMessages_thatCorrespondToMethodSuppressedWarning()
            throws IllegalAccessException, InstantiationException {
        String suppressedRuleKey = "2116";
        String nonSuppressedRuleKey = "2164";
        String testFile =
                Paths.get(Constants.PATH_TO_RESOURCES_FOLDER)
                        .resolve("WithMethodSuppressedS" + suppressedRuleKey + ".java")
                        .toString();

        var violations =
                RuleVerifier.analyze(
                        List.of(testFile),
                        new File(Constants.PATH_TO_RESOURCES_FOLDER),
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
                Paths.get(Constants.PATH_TO_RESOURCES_FOLDER)
                        .resolve("ArrayHashCodeAndToString.java")
                        .toString();
        var violations =
                RuleVerifier.analyze(
                        List.of(testFile),
                        new File(Constants.PATH_TO_RESOURCES_FOLDER),
                        checkWithNoLocation);

        assertThat(violations, is(empty()));
    }

    @Test
    public void analyze_doesNotReturnViolation_fromNosonarLine() throws IOException {
        // arrange
        Path testFile =
                Paths.get(Constants.PATH_TO_RESOURCES_FOLDER).resolve("NOSONARCommentTest.java");
        int nosonarLine = 7;
        int violationLine = 8;
        List<String> lines = Files.readAllLines(testFile);
        assertThat(lines.get(nosonarLine - 1), containsString("// Noncompliant, NOSONAR"));
        assertThat(lines.get(violationLine - 1), containsString("// Noncompliant"));

        // act
        var violations =
                RuleVerifier.analyze(
                        List.of(testFile.toString()),
                        new File(Constants.PATH_TO_RESOURCES_FOLDER),
                        List.of(Checks.getCheckInstance("S2116")));

        // assert
        assertThat(violations.size(), equalTo(1));
        assertThat(violations.stream().findFirst().get().getStartLine(), equalTo(violationLine));
    }
}
