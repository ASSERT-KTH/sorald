package sorald.processor;

import java.io.Serializable;
import java.util.Set;
import sorald.annotations.IncompleteProcessor;
import sorald.annotations.ProcessorAnnotation;
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
                "Every class implementing Serializable should declare a private static final serialVersionUID.")
public class SerialVersionUidProcessor extends SoraldAbstractProcessor<CtClass<?>> {

    private static final String SERIAL_VERSION_UID = "serialVersionUID";
    private Set<ModifierKind> modifiers =
            Set.of(ModifierKind.PRIVATE, ModifierKind.STATIC, ModifierKind.FINAL);

    @Override
    protected boolean canRepairInternal(CtClass<?> candidate) {
        Set<CtTypeReference<?>> superInterfaces = candidate.getSuperInterfaces();
        CtTypeReference<?> serializable =
                candidate.getFactory().createCtTypeReference(Serializable.class);
        // check if the class implements serializable
        if (!superInterfaces.contains(serializable)) {
            // no serializable found => nothing to repair
            return false;
        }
        CtFieldReference<?> fieldReference = getSerialVersionUIDField(candidate);
        // class implements serializable, lets check for serialVersionUID field
        // the type of the field can be anything here, but should be long
        if (fieldReference != null) {
            CtField<?> field = fieldReference.getDeclaration();
            // check the type
            if (!isLongType(field)) {
                // serialVersionUID found but type is not long
                // => we cant fix it
                return false;
            }
            // check all modifiers, the field should be private static final
            return !hasCorrectModifier(field);
        }
        // here should the class implement serializable and doesn't have a serialVersionUID with
        // correct type
        return true;
    }

    private boolean hasCorrectModifier(CtField<?> field) {
        return field.isFinal() && field.isPrivate() && field.isStatic();
    }

    private boolean isLongType(CtField<?> field) {
        return field.getType().equals(getLongPrimitiveType(field));
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
        if (serialVersionUidReference == null) {
            // serialVersionUID Field missing => add the field
            CtField<?> replacement =
                    element.getFactory()
                            .createField(
                                    null,
                                    modifiers,
                                    getLongPrimitiveType(element),
                                    SERIAL_VERSION_UID);
            replacement.setDefaultExpression(
                    element.getFactory().createCodeSnippetExpression("1L"));
            element.addFieldAtTop(replacement);
        } else {
            // in this case the field exists but the modifiers are wrong.
            element.setModifiers(modifiers);
        }
    }
}
