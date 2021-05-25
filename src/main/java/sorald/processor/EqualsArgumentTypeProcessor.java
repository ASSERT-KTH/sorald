package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.declaration.CtMethod;

@ProcessorAnnotation(key = 2097, description = "\"equals(Object obj)\" should test argument type")
public class EqualsArgumentTypeProcessor extends SoraldAbstractProcessor<CtMethod<?>> {

    @Override
    protected void repairInternal(CtMethod<?> element) {}
}
