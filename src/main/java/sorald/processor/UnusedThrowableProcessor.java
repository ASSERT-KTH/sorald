package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtThrow;

@ProcessorAnnotation(
        key = 3984,
        description = "Exception should not be created without being thrown")
public class UnusedThrowableProcessor extends SoraldAbstractProcessor<CtConstructorCall> {

    public UnusedThrowableProcessor() {}

    @Override
    public boolean isToBeProcessed(CtConstructorCall element) {
        if (!super.isToBeProcessedAccordingToStandards(element)) {
            return false;
        }
        return true;
    }

    @Override
    public void process(CtConstructorCall element) {
        super.process(element);

        CtThrow ctThrow = getFactory().createCtThrow(element.toString());
        element.replace(ctThrow);
    }
}
