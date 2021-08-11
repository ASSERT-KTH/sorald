package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.declaration.CtField;

// @spotless:off
/**
 * The repair consists of deleting unused `private` fields as it is considered as dead code.
 *
 * Example:
 *
 * ```diff
 *  public class UnusedPrivateField {
 * -    private String a = "Hello world!";
 *
 *      public int compute(int number) {
 *          return number * 42;
 *      }
 *  }
 * ```
 *
 * However, the `serialVersionUID` field, which must be `private`, `static`, `final`, and of type `long`, in Serializable
 * classes is not deleted because it is used during deserialization of byte stream.
 */
// @spotless:on
@ProcessorAnnotation(key = "S1068", description = "Unused \"private\" fields should be removed")
public class UnusedPrivateFieldProcessor extends SoraldAbstractProcessor<CtField<?>> {
    @Override
    protected void repairInternal(CtField<?> element) {
        element.delete();
    }
}
