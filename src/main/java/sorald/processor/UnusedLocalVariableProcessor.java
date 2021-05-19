package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtLocalVariable;

@ProcessorAnnotation(key = 1481, description = "Unused local variables should be removed")
public class UnusedLocalVariableProcessor extends SoraldAbstractProcessor<CtLocalVariable<?>> {

    @Override
    protected void repairInternal(CtLocalVariable<?> element) {
        element.delete();
    }
}
