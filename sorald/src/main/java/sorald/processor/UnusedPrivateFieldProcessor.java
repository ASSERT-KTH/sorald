package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import sorald.api.SoraldAbstractProcessor;
import spoon.reflect.declaration.CtField;

@ProcessorAnnotation(key = "S1068", description = "Unused \"private\" fields should be removed")
public class UnusedPrivateFieldProcessor extends SoraldAbstractProcessor<CtField<?>> {
    @Override
    protected void repairInternal(CtField<?> element) {
        element.delete();
    }
}
