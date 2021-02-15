package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtStatement;

@ProcessorAnnotation(key = 1854, description = "Unused assignments should be removed")
public class DeadStoreProcessor extends SoraldAbstractProcessor<CtStatement> {

    @Override
    protected void repairInternal(CtStatement element) {
        element.delete();
    }
}
