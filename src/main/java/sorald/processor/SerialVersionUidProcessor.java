package sorald.processor;

import java.util.Set;
import sorald.annotations.IncompleteProcessor;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;

@IncompleteProcessor(
        description =
                "This processor does not address the case where the class already has a serialVersionUID with a non long type.")
@ProcessorAnnotation(
        key = 2057,
        description =
                "Every class implementing Serializable should declare a static final serialVersionUID.")
public class SerialVersionUidProcessor extends SoraldAbstractProcessor<CtClass<?>> {

    private static final String DEFAULT_ID_VALUE = "1L";
    private static final String SERIAL_VERSION_UID = "serialVersionUID";
    private final Set<ModifierKind> modifiers = Set.of(ModifierKind.STATIC, ModifierKind.FINAL);

    @Override
    protected boolean canRepairInternal(CtClass<?> candidate) {
        CtField<?> serialVersionUIDField = getDirectlyDeclaredSerialVersionUIDField(candidate);
        return serialVersionUIDField == null || isLongType(serialVersionUIDField);
    }

    private boolean isLongType(CtField<?> field) {
        return field.getType().equals(getLongPrimitiveType(field));
    }

    private CtField<?> getDirectlyDeclaredSerialVersionUIDField(CtClass<?> candidate) {
        return candidate.getFields().stream()
                .filter(v -> v.getSimpleName().equals(SERIAL_VERSION_UID))
                .findFirst()
                .orElse(null);
    }

    private CtTypeReference<Long> getLongPrimitiveType(CtElement element) {
        return element.getFactory().Type().longPrimitiveType();
    }

    @Override
    protected void repairInternal(CtClass<?> element) {
        CtField<?> serialVersionUIDField = getDirectlyDeclaredSerialVersionUIDField(element);
        CtField<?> replacement =
                element.getFactory()
                        .createField(
                                null, modifiers, getLongPrimitiveType(element), SERIAL_VERSION_UID);
        replacement.setDefaultExpression(
                element.getFactory().createCodeSnippetExpression(DEFAULT_ID_VALUE));

        if (serialVersionUIDField != null) {
            // we add the old modifiers here because sonar only forces final and static but a user
            // could
            // add more modifiers.
            serialVersionUIDField.getModifiers().forEach(replacement::addModifier);
            replacement.setComments(serialVersionUIDField.getComments());
            CtExpression<?> expression = serialVersionUIDField.getDefaultExpression();
            if (expression != null) {
                replacement.setDefaultExpression(
                        element.getFactory().createCodeSnippetExpression(expression.toString()));
            }
            serialVersionUIDField.replace(replacement);
        } else {
            element.addFieldAtTop(replacement);
        }
    }
}
