package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;

@ProcessorAnnotation(
        key = 1948,
        description =
                "Fields in a \"Serializable\" class should either be transient or serializable")
public class SerializableFieldInSerializableClassProcessor
        extends SoraldAbstractProcessor<CtField> {

    public SerializableFieldInSerializableClassProcessor() {}

    @Override
    public boolean isToBeProcessed(CtField element) {
        if (!super.isToBeProcessedAccordingToStandards(element)) {
            return false;
        }
        return true;
    }

    @Override
    public void process(CtField element) {
        super.process(element);
        element.addModifier(ModifierKind.TRANSIENT);
    }
}
