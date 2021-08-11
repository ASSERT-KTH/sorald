package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtLocalVariable;

// @spotless:off
/**
 * The repair consists of deleting unused local variables. This largely overlaps
 * with rule 1854 "Unused assignments should be removed", but covers some
 * additional cases. For example, loop header variables are not considered by
 * rule 1854 for some reason, but they are included in this rule.
 *
 * Example repair where a variable declared in a loop header is unused:
 *
 * ```diff
 *  public static void main(String[] args) {
 * -    for (int x = 0, y = 0, z = 10; x <= z; x++) { // Noncompliant, y is not used
 * +    for (int x = 0, z = 10; x <= z; x++) { // Noncompliant, y is not used
 *         System.out.println("Current: " + x);
 *         System.out.println("Goal: " + z);
 *     }
 * }
 * ```
 */
// @spotless:on
@ProcessorAnnotation(key = "S1481", description = "Unused local variables should be removed")
public class UnusedLocalVariableProcessor extends SoraldAbstractProcessor<CtLocalVariable<?>> {

    @Override
    protected void repairInternal(CtLocalVariable<?> element) {
        element.delete();
    }
}
