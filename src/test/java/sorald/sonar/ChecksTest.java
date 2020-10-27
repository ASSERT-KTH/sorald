package sorald.sonar;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.java.checks.CompareStringsBoxedTypesWithEqualsCheck;
import org.sonar.java.checks.DeadStoreCheck;
import org.sonar.java.checks.InputStreamReadCheck;
import org.sonar.java.checks.NullShouldNotBeUsedWithOptionalCheck;
import org.sonar.java.checks.ObjectFinalizeCheck;
import org.sonar.java.checks.serialization.SerializableFieldInSerializableClassCheck;
import org.sonar.plugins.java.api.JavaFileScanner;

class ChecksTest {

    @Test
    void test_getChecksByType_whenTypeIsBug_containsBugChecks() {
        final List<Class<? extends JavaFileScanner>> bugChecks =
                Checks.getChecksByType(Checks.CheckType.BUG);
        final List<Class<? extends JavaFileScanner>> expectedBugChecksSubset =
                Arrays.asList(
                        CompareStringsBoxedTypesWithEqualsCheck.class,
                        InputStreamReadCheck.class,
                        NullShouldNotBeUsedWithOptionalCheck.class,
                        ObjectFinalizeCheck.class);

        assertTrue(bugChecks.containsAll(expectedBugChecksSubset));
    }

    @Test
    void test_getChecksByType_whenTypeIsCodeSmell_containsCodeSmellChecks() {
        final List<Class<? extends JavaFileScanner>> codeSmellChecks =
                Checks.getChecksByType(Checks.CheckType.CODE_SMELL);
        final List<Class<? extends JavaFileScanner>> expectedCodeSmellChecksSubset =
                Arrays.asList(
                        DeadStoreCheck.class, SerializableFieldInSerializableClassCheck.class);

        assertTrue(codeSmellChecks.containsAll(expectedCodeSmellChecksSubset));
    }
}
