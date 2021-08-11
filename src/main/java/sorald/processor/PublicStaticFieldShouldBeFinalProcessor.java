package sorald.processor;

import sorald.annotations.IncompleteProcessor;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;

/**
 * The repair consists of making public static fields final.
 *
 * Example:
 * ```diff
 *  public class NonFinalPublicStaticField {
 * -    public static Integer meaningOfLife = 42;
 * +    public static final Integer meaningOfLife = 42;
 *      private static Integer CATCH = 22; // Compliant
 *      protected static Integer order = 66; // Compliant
 *      static Integer roadToHill = 30; // Compliant
 *  }
 * ```
 */
@IncompleteProcessor(description = "does not fix variable naming")
@ProcessorAnnotation(key = "S1444", description = "\"public static\" fields should be constant")
public class PublicStaticFieldShouldBeFinalProcessor extends SoraldAbstractProcessor<CtField<?>> {
    @Override
    protected void repairInternal(CtField<?> element) {
        element.addModifier(ModifierKind.FINAL);
    }
}
