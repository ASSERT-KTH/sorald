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

    @Override
    public boolean canRepairInternal(CtField element) {
        return true;
    }

    @Override
    public void repairInternal(CtField element) {
        element.addModifier(ModifierKind.TRANSIENT);
    }
}
