package sorald.processor;

import sorald.ProcessorAnnotation;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;

@ProcessorAnnotation(key = 2755, description = "XML parsers should not be vulnerable to XXE attacks")
public class XxeProcessingProcessor extends SoraldAbstractProcessor<CtField<?>> {
    @Override
    public boolean isToBeProcessed(CtField<?> candidate) {
        return true;
    }

    @Override
    public void process(CtField<?> element) {
    }
}
