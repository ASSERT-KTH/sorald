package sorald.processor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import sorald.event.StatisticsCollector;
import spoon.Launcher;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

/** Tests for the concrete methods of {@link sorald.processor.SoraldAbstractProcessor}. */
public class SoraldAbstractProcessorTest {
    private static final RuntimeException EXCEPTION = new RuntimeException("I'm a crash :)");

    @Test
    public void canRepair_returnsFalse_whenInternalMethodCrashes() {
        assertFalse(new CrashyProcessor().canRepair(getObjectToString()));
    }

    @Test
    public void canRepair_recordsCrashEvent() throws IOException {
        var crashyProcessor = new CrashyProcessor();
        var statsCollector = new StatisticsCollector();
        CtMethod<?> objectToString = getObjectToString();

        crashyProcessor.setEventHandlers(List.of(statsCollector));

        crashyProcessor.canRepair(objectToString);

        assertThat(statsCollector.getCrashes().size(), equalTo(1));
    }

    /** Processor that always crashes. */
    private static class CrashyProcessor extends SoraldAbstractProcessor<CtMethod<?>> {
        @Override
        protected boolean canRepairInternal(CtMethod<?> candidate) {
            throw EXCEPTION;
        }

        @Override
        public void repair(CtMethod<?> element) {}
    }

    private static CtMethod<?> getObjectToString() {
        Launcher launcher = new Launcher();
        CtType<?> type = launcher.getFactory().Type().OBJECT.getTypeDeclaration();
        return type.getMethod("toString");
    }
}
