package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;

@ProcessorAnnotation(key = 1854, description = "Unused assignments should be removed")
public class DeadStoreProcessor extends SoraldAbstractProcessor<CtStatement> {

    @Override
    protected boolean canRepairInternal(CtStatement element) {
        if (element instanceof CtLocalVariable || element instanceof CtAssignment) {
            return true;
        }
        return false;
    }

    @Override
    protected void repairInternal(CtStatement element) {
        element.delete();
    }
}
