package sorald.processor;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import spoon.reflect.code.CtInvocation;

/** Tests for the concrete methods of {@link sorald.processor.SoraldAbstractProcessor}. */
public class SoraldAbstractProcessorTest {

    @Test
    public void canRepair_returnsFalse_whenInternalMethodCrashes() {
        assertFalse(new CrashyProcessor().canRepair(null));
    }

    /** Processor that always crashes. */
    private static class CrashyProcessor extends SoraldAbstractProcessor<CtInvocation<?>> {
        @Override
        protected boolean canRepairInternal(CtInvocation<?> candidate) {
            throw new RuntimeException("I'm crashy :)");
        }

        @Override
        public void repair(CtInvocation<?> element) {}
    }
}
