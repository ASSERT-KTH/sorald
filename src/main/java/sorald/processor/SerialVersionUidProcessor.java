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

    private static final String DEFAULT_ID_VALUE = "1L";
    private static final String SERIAL_VERSION_UID = "serialVersionUID";
    private final Set<ModifierKind> modifiers = Set.of(ModifierKind.STATIC, ModifierKind.FINAL);

    @Override
    protected boolean canRepairInternal(CtClass<?> candidate) {
        Set<CtTypeReference<?>> superInterfaces = candidate.getSuperInterfaces();
        if (isException(candidate)) {
            return false;
        }
        CtTypeReference<?> serializable =
                candidate.getFactory().createCtTypeReference(Serializable.class);
        if (superInterfaces.contains(serializable)) {

            CtFieldReference<?> fieldReference = getSerialVersionUIDField(candidate);

            if (fieldReference != null) {
                CtField<?> field = fieldReference.getDeclaration();
                if (!isLongType(field)) {
                    // Here is the special case where the field exisits but it's type is wrong.
                    // TODO: handle this case after discussion.
                    return false;
                }
                return !hasCorrectModifier(field);
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a given class ia a expection for having an ID. SonarQube states 4 expections
     *
     * <ul>
     *   <li>Swing and AWT classes (GUI classes)
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
    /**
     * Tests if a given class is a gui class. A gui class for sonarqube is a class having a swing or
     * awt class/interface as supertype. See
     * https://github.com/SonarSource/sonar-java/blob/master/java-checks/src/main/java/org/sonar/java/checks/serialization/SerialVersionUidCheck.java#L66
     * for their code
     *
     * @param candidate
     * @return true if gui class false otherwise
     */
    private boolean isGuiClass(CtClass<?> candidate) {
        Set<CtTypeReference<?>> superTypes = new HashSet<>();
        superTypes.add(candidate.getReference());
        CtType<?> currentClass = candidate;
        while (currentClass != null) {
            superTypes.addAll(currentClass.getSuperInterfaces());
            superTypes.add(currentClass.getSuperclass());
            if (currentClass.getSuperclass() != null) {
                currentClass = currentClass.getSuperclass().getTypeDeclaration();
            } else {
                currentClass = null;
            }
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
    /**
     * checks if a given class extends {@link Throwable}
     *
     * @param candidate
     * @return if subtype of throwable false otherwise
     */
    private boolean extendsThrowable(CtClass<?> candidate) {
        return candidate.isSubtypeOf(candidate.getFactory().createCtTypeReference(Throwable.class));
    }

    /**
     * Checks if a given uid field has the correct identifiers. Sonarqube only enforces static and
     * final.
     * https://github.com/SonarSource/sonar-java/blob/149242d6651c797c73584615faedc8921b1bc435/java-checks/src/main/java/org/sonar/java/checks/serialization/SerialVersionUidCheck.java#L66
     *
     * @param field
     * @return true if the modifiers are correct, false otherwise.
     */
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
        CtField<?> replacement =
                element.getFactory()
                        .createField(
                                null, modifiers, getLongPrimitiveType(element), SERIAL_VERSION_UID);
        replacement.setDefaultExpression(
                element.getFactory().createCodeSnippetExpression(DEFAULT_ID_VALUE));

        if (serialVersionUidReference != null) {
            CtField<?> oldField = serialVersionUidReference.getDeclaration();
            replacement.setComments(oldField.getComments());
            oldField.replace(replacement);
        } else {
            element.addFieldAtTop(replacement);
        }
    }
}
