package sorald.sonar;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.sonar.java.AnalyzerMessage;
import org.sonar.java.checks.ArrayHashCodeAndToStringCheck;

class ScannedViolationTest {

    @Test
    public void constructor_throws_whenPrimaryLocationIsNull() {
        var message =
                new AnalyzerMessage(
                        new ArrayHashCodeAndToStringCheck(), null, -1, "bogus message", 0);

        assertThrows(IllegalArgumentException.class, () -> new ScannedViolation(message));
    }
}
