package sorald.processor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import org.junit.jupiter.api.Test;
import sorald.event.collectors.RepairStatisticsCollector;
import spoon.Launcher;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

/** Tests for the concrete methods of {@link sorald.processor.SoraldAbstractProcessor}. */
public class SoraldAbstractProcessorTest {
    private static final RuntimeException EXCEPTION = new RuntimeException("I'm a crash :)");

    @Test
    public void canRepair_returnsFalse_whenInternalMethodCrashes() {
        var crashyProcessor =
                new CrashyProcessor().setEventHandlers(List.of(new RepairStatisticsCollector()));
        assertFalse(crashyProcessor.canRepair(getObjectToString()));
    }

    @Test
    public void canRepair_recordsCrashEvent() {
        var crashyProcessor = new CrashyProcessor();
        var statsCollector = new RepairStatisticsCollector();
        CtMethod<?> objectToString = getObjectToString();

        crashyProcessor.setEventHandlers(List.of(statsCollector));

        crashyProcessor.canRepair(objectToString);

        assertThat(statsCollector.getCrashes().size(), equalTo(1));
    }

    @Test
    public void repair_returnsFalse_whenInternalMethodCrashes() {
        var crashyProcessor =
                new CrashyProcessor().setEventHandlers(List.of(new RepairStatisticsCollector()));
        assertFalse(crashyProcessor.repair(getObjectToString()));
    }

    @Test
    public void repair_recordsCrashEvent() {
        var statsCollector = new RepairStatisticsCollector();
        var crashyProcessor = new CrashyProcessor().setEventHandlers(List.of(statsCollector));
        CtMethod<?> objectToString = getObjectToString();

        crashyProcessor.repair(objectToString);

        assertThat(statsCollector.getCrashes().size(), equalTo(1));
    }

    @Test
    public void process_recordsCrashEvent() {
        var statsCollector = new RepairStatisticsCollector();
        var crashyProcessor = new CrashyProcessor().setEventHandlers(List.of(statsCollector));

        // this will crash as we haven't set the bestFits for the processor
        crashyProcessor.process(getObjectToString());

        assertThat(statsCollector.getCrashes().size(), equalTo(1));
    }

    /** Processor that always crashes. */
    private static class CrashyProcessor extends SoraldAbstractProcessor<CtMethod<?>> {
        @Override
        protected boolean canRepairInternal(CtMethod<?> candidate) {
            throw EXCEPTION;
        }

        @Override
        public void repairInternal(CtMethod<?> element) {
            throw EXCEPTION;
        }
    }

    private static CtMethod<?> getObjectToString() {
        Launcher launcher = new Launcher();
        CtType<?> type = launcher.getFactory().Type().OBJECT.getTypeDeclaration();
        return type.getMethod("toString");
    }
}
