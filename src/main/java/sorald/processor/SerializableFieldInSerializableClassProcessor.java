package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;

// @spotless:off
/**
 * The repair adds the modifier `transient` to all non-serializable
 * fields. In the future, the plan is to give user the option if they want to go to the class
 * of that field and add `implements Serializable` to it.
 *
 * Example:
 * ```diff
 *  public class SerializableFieldProcessorTest implements Serializable {
 * -    private Unser uns;// Noncompliant
 * +    private transient Unser uns;
 * ```
 *
 * Check out an accepted PR in [Spoon](https://github.com/INRIA/spoon/pull/2121) that repairs three SerializableFieldInSerializableClass violations.
 */
// @spotless:on
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
