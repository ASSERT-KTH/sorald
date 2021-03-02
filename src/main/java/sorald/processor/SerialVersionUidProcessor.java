package sorald.processor;

import java.util.Set;
import sorald.annotations.IncompleteProcessor;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

@IncompleteProcessor(
        description =
                "This processor ignores two corner cases: If the class has already a serialVersionUID with a non long type and if the class is not directly implementing serializable e.g. a upper class implements serializable.")
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
        CtFieldReference<?> fieldReference = getSerialVersionUIDField(candidate);
        return fieldReference == null || isLongType(fieldReference);
    }

    private boolean isLongType(CtFieldReference<?> fieldRef) {
        return fieldRef.getType().equals(getLongPrimitiveType(fieldRef));
    }

    private CtFieldReference<?> getSerialVersionUIDField(CtClass<?> candidate) {
        return candidate.getAllFields().stream()
                .filter(v -> v.getSimpleName().equals(SERIAL_VERSION_UID))
                .findFirst()
                .orElse(null);
    }

    private CtTypeReference<Long> getLongPrimitiveType(CtElement element) {
        return element.getFactory().Type().longPrimitiveType();
    }

    @Override
    protected void repairInternal(CtClass<?> element) {
        CtFieldReference<?> serialVersionUidReference = getSerialVersionUIDField(element);
        CtField<?> replacement =
                element.getFactory()
                        .createField(
                                null, modifiers, getLongPrimitiveType(element), SERIAL_VERSION_UID);
        replacement.setDefaultExpression(
                element.getFactory().createCodeSnippetExpression(DEFAULT_ID_VALUE));

        if (serialVersionUidReference != null) {
            CtField<?> oldField = serialVersionUidReference.getDeclaration();
            // we add the old modifiers here because sonar only forces final and static but a user
            // could
            // add more modifiers.
            oldField.getModifiers().forEach(replacement::addModifier);
            replacement.setComments(oldField.getComments());
            CtExpression<?> expression = oldField.getDefaultExpression();
            if (expression != null) {
                replacement.setDefaultExpression(
                        element.getFactory().createCodeSnippetExpression(expression.toString()));
            }
            oldField.replace(replacement);
        } else {
            element.addFieldAtTop(replacement);
        }
    }
}
