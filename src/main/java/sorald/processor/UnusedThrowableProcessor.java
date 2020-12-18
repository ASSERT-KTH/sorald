package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtThrow;

@ProcessorAnnotation(
        key = 3984,
        description = "Exception should not be created without being thrown")
public class UnusedThrowableProcessor extends SoraldAbstractProcessor<CtConstructorCall> {

    @Override
    protected boolean canRepairInternal(CtConstructorCall element) {
        return true;
    }

    @Override
    protected void repairInternal(CtConstructorCall element) {
        CtThrow ctThrow = getFactory().createCtThrow(element.toString());
        element.replace(ctThrow);
    }
}
