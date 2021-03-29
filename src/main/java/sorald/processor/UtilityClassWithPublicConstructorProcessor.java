package sorald.processor;

import sorald.annotations.IncompleteProcessor;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.declaration.CtClass;

@IncompleteProcessor(description = "Only handles implicit public constructor")
@ProcessorAnnotation(
        key = 1118,
        description = "Utility classes should not have public constructors")
public class UtilityClassWithPublicConstructorProcessor
        extends SoraldAbstractProcessor<CtClass<?>> {
    @Override
    protected void repairInternal(CtClass<?> element) {}
}
