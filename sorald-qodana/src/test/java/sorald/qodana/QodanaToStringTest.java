package sorald.qodana;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;

public class QodanaToStringTest {

    @Test
    public void test() {
        var result =
                new Qodana()
                        .findViolations(
                                new File("./src/test/resources/simpleProject"),
                                List.of(),
                                List.of(),
                                List.of());
        assertFalse(result.isEmpty(), "There should be violations");
    }
}
