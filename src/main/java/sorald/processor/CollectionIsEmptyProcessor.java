package sorald.processor;

import static sorald.util.Transformations.not;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;

// @spotless:off
/**
 * Using `Collection.size()` to test for emptiness works, but using `Collection.isEmpty()` makes the code more readable and
 * can be more performant. Expressions `myCollection.size() == 0` are replaced by `myCollection.isEmpty()`, and
 * expressions `myCollection.size() != 0` are replaced by `!myCollection.isEmpty()`.
 *
 * Example:
 * ```diff
 * - if (myCollection.size() == 0) {  // Noncompliant
 * + if (myCollection.isEmpty()) {
 * ...
 * - if (myCollection.size() != 0) {  // Noncompliant
 * + if (!myCollection.isEmpty()) {
 * ```
 */
// @spotless:on
@ProcessorAnnotation(
        key = "S1155",
        description = "Collection.isEmpty() should be used to test for emptiness")
public class CollectionIsEmptyProcessor extends SoraldAbstractProcessor<CtBinaryOperator<?>> {

    @Override
    protected void repairInternal(CtBinaryOperator<?> element) {
        CtExpression<?> methodCallTarget =
                ((CtInvocation<?>) element.getLeftHandOperand()).getTarget();

        CtMethod<?> isEmptyMethod =
                methodCallTarget.getType().getTypeDeclaration().getMethodsByName("isEmpty").stream()
                        .findFirst()
                        .orElseThrow(IllegalStateException::new);
        CtInvocation<?> newInvocation =
                getFactory().createInvocation(methodCallTarget, isEmptyMethod.getReference());
        CtExpression<?> expression =
                element.getKind() == BinaryOperatorKind.EQ ? newInvocation : not(newInvocation);
        element.replace(expression);
    }
}
