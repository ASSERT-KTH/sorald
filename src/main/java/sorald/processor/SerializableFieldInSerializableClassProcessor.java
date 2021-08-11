package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;

@ProcessorAnnotation(
        key = "S1948",
        description =
                "Fields in a \"Serializable\" class should either be transient or serializable")
public class SerializableFieldInSerializableClassProcessor
        extends SoraldAbstractProcessor<CtField> {
    @Override
    protected void repairInternal(CtField element) {
        element.addModifier(ModifierKind.TRANSIENT);
    }
}
