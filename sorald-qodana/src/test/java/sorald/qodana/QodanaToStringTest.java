package sorald.qodana;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import sorald.qodana.processors.StringOperationCanBeSimplifiedProcessor;
import sorald.qodana.rules.QodanaRules;
import sorald.rule.RuleViolation;
import sorald.sonar.BestFitScanner;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

public class QodanaToStringTest {

    @Test
    public void stringOperationCanBeSimplified() {
        Collection<RuleViolation> result =
                new Qodana()
                        .findViolations(
                                new File("./src/test/resources/simpleProject"),
                                List.of(
                                        Path.of(
                                                        "src/test/resources/simpleProject/src/main/java/ToStringTest.java")
                                                .toFile()),
                                List.of(QodanaRules.STRING_OPERATION_CAN_BE_SIMPLIFIED),
                                List.of());
        assertFalse(result.isEmpty(), "There should be violations");
        // remove violations that are not relevant to this test.
        result.removeIf(v -> !v.getRuleKey().equals("StringOperationCanBeSimplified"));
        Launcher launcher = new Launcher();
        launcher.addInputResource("src/test/resources/simpleProject");
        CtModel model = launcher.buildModel();
        CtType<Object> testFile = getTypeWithQualifiedName(model, "ToStringTest");
        var bestFits =
                BestFitScanner.calculateBestFits(
                        testFile,
                        new HashSet<>(result),
                        new StringOperationCanBeSimplifiedProcessor());
        assertEquals(
                1, bestFits.size(), "There should be only one String could be simplified error");
        CtInvocation<?> invocation = (CtInvocation<?>) bestFits.keySet().iterator().next();
        assertTrue(
                new StringOperationCanBeSimplifiedProcessor().repair(invocation),
                "The repair should not fail");
        assertTrue(checkNoToStringExistsAnyMore(invocation), "No toString call should be left");
    }

    private boolean checkNoToStringExistsAnyMore(CtInvocation<?> invocation) {
        return invocation.getElements(new TypeFilter<>(CtInvocation.class)).stream()
                .noneMatch(v -> v.getExecutable().getSimpleName().equals("toString"));
    }

    private CtType<Object> getTypeWithQualifiedName(CtModel model, String fqName) {
        return model.getRootPackage().getFactory().Type().get(fqName);
    }
}
