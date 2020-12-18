package sorald.processor;

import sorald.annotations.IncompleteProcessor;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;

@IncompleteProcessor(description = "does not fix variable naming")
@ProcessorAnnotation(key = 1444, description = "\"public static\" fields should be constant")
public class PublicStaticFieldShouldBeFinalProcessor extends SoraldAbstractProcessor<CtField<?>> {
    @Override
    protected boolean canRepairInternal(CtField<?> candidate) {
        return true;
    }

    @Override
    protected void repairInternal(CtField<?> element) {
        element.addModifier(ModifierKind.FINAL);
    }
}
