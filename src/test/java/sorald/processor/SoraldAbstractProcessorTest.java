package sorald.processor;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import spoon.reflect.code.CtInvocation;

/** Tests for the concrete methods of {@link sorald.processor.SoraldAbstractProcessor}. */
public class SoraldAbstractProcessorTest {

    @Test
    public void canRepair_returnsFalse_whenInternalMethodCrashes() {
        SoraldAbstractProcessor<?> crashyProcessor =
                new SoraldAbstractProcessor<CtInvocation<?>>() {
                    @Override
                    protected boolean canRepairInternal(CtInvocation<?> candidate) {
                        throw new RuntimeException("I'm crashy :)");
                    }

                    @Override
                    public void repair(CtInvocation<?> element) {}
                };

        assertFalse(crashyProcessor.canRepair(null));
    }
}
