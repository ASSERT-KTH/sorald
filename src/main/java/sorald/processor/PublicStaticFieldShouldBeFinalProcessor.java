package sorald.processor;

import sorald.ProcessorAnnotation;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;

@ProcessorAnnotation(key = 1444, description = "\"public static\" fields should be constant")
public class PublicStaticFieldShouldBeFinalProcessor extends SoraldAbstractProcessor<CtField<?>> {
    @Override
    public boolean isToBeProcessed(CtField<?> candidate) {
        return super.isToBeProcessedAccordingToStandards(candidate);
    }

    @Override
    public void process(CtField<?> element) {
        super.process(element);
        element.addModifier(ModifierKind.FINAL);
    }
}
