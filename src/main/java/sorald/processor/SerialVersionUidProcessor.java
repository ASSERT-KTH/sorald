package sorald.processor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import sorald.annotations.IncompleteProcessor;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;
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

    private static final String SERIAL_VERSION_UID = "serialVersionUID";
    private Set<ModifierKind> modifiers = Set.of(ModifierKind.STATIC, ModifierKind.FINAL);

    @Override
    protected boolean canRepairInternal(CtClass<?> candidate) {
        Set<CtTypeReference<?>> superInterfaces = candidate.getSuperInterfaces();
        if (isException(candidate)) {
            return false;
        }
        CtTypeReference<?> serializable =
                candidate.getFactory().createCtTypeReference(Serializable.class);
        // check if the class implements serializable
        if (superInterfaces.contains(serializable)) {

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
            } else {
                // the field is missing we can add it
                return true;
            }
        }
        // class does not implement serializable
        return false;
    }

    /**
     * Checks if a given class ia a expection for having an ID. SonarQube states 4 expections
     *
     * <ul>
     *   <li>Swing and AWT classes
     *   <li>abstract classes
     *   <li>Throwable and its subclasses (Exceptions and Errors)
     *   <li>classes marked with @SuppressWarnings("serial")
     * </ul>
     *
     * @param candidate
     * @return true if the class should be ignored
     */
    private boolean isException(CtClass<?> candidate) {

        return candidate.isAbstract()
                || extendsThrowable(candidate)
                || supressesWarning(candidate)
                || isGuiClass(candidate);
    }
    // looking at the code
    // https://github.com/SonarSource/sonar-java/blob/master/java-checks/src/main/java/org/sonar/java/checks/serialization/SerialVersionUidCheck.java#L66
    // a gui class is a class extending any awt or swing type
    private boolean isGuiClass(CtClass<?> candidate) {
        Set<CtTypeReference<?>> superTypes = new HashSet<>();
        superTypes.add(candidate.getReference());
        CtType<?> currentClass = candidate;
        while (currentClass != null) {
            superTypes.addAll(currentClass.getSuperInterfaces());
            superTypes.add(currentClass.getSuperclass());
            currentClass = currentClass.getSuperclass().getTypeDeclaration();
        }
        superTypes.removeIf(Objects::isNull);
        for (CtTypeReference<?> ctTypeReference : superTypes) {
            if (ctTypeReference.getQualifiedName().startsWith("java.awt")
                    || ctTypeReference.getQualifiedName().startsWith("javax.swing")) {
                return true;
            }
        }
        return false;
    }
    /**
     * checks if a given class has a @SupressWarnings("serial") annotation
     *
     * @param candidate
     * @return true if present false otherwise
     */
    private boolean supressesWarning(CtClass<?> candidate) {
        SuppressWarnings annotation = candidate.getAnnotation(SuppressWarnings.class);
        if (annotation != null
                && Arrays.stream(annotation.value()).anyMatch(v -> v.equals("serial"))) {
            return true;
        }
        return false;
    }

    private boolean extendsThrowable(CtClass<?> candidate) {
        return candidate.isSubtypeOf(candidate.getFactory().createCtTypeReference(Throwable.class));
    }

    // strangly sonarcube only checks for final and static, so we do
    // https://github.com/SonarSource/sonar-java/blob/149242d6651c797c73584615faedc8921b1bc435/java-checks/src/main/java/org/sonar/java/checks/serialization/SerialVersionUidCheck.java#L66
    private boolean hasCorrectModifier(CtField<?> field) {
        return field.isFinal() && field.isStatic();
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
        // serialVersionUID Field missing or identifiers are wrong, we simply add a fixed field as
        // replacement
        CtField<?> replacement =
                element.getFactory()
                        .createField(
                                null, modifiers, getLongPrimitiveType(element), SERIAL_VERSION_UID);
        replacement.setDefaultExpression(element.getFactory().createCodeSnippetExpression("1L"));
        if (serialVersionUidReference != null) {
            // we add the field at the old position
            CtField<?> oldField = serialVersionUidReference.getDeclaration();
            replacement.setComments(oldField.getComments());
            int position = element.getTypeMembers().indexOf(oldField);
            element.removeTypeMember(oldField);
            element.addTypeMemberAt(position, replacement);
        } else {
            // we have no known position so we add it to the top
            element.addFieldAtTop(replacement);
        }
    }
}
