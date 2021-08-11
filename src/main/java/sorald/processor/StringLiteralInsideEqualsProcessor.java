package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtExecutableReference;

// @spotless:off
/**
 * String comparisons by using `equals` or `equalsIgnoreCase` in which the target for the method call is a variable and the argument is a literal are changed by swapping the variable and the literal. This avoids potential null pointer exceptions.
 *
 * Example:
 * ```diff
 * - System.out.println("Equal? " + myString.equals("foo")); // Noncompliant; can raise a NPE
 * + System.out.println("Equal? " + "foo".equals(myString));
 * ```
 *
 * When there is a null check on the variable, it is removed.
 *
 * Example:
 * ```diff
 * - System.out.println("Equal? " + (myString != null && myString.equals("foo"))); // Noncompliant
 * + System.out.println("Equal? " + "foo".equals(myString));
 * ```
 */
// @spotless:on
@ProcessorAnnotation(
        key = "S1132",
        description =
                "Strings literals should be placed on the left side when checking for equality")
public class StringLiteralInsideEqualsProcessor extends SoraldAbstractProcessor<CtInvocation<?>> {

    @Override
    protected void repairInternal(CtInvocation<?> element) {
        // Get executable reference of method to be called
        CtExecutableReference<?> ctExecutableReferenceToMethodToBeCalled = element.getExecutable();

        // Create a new invocation in which the receiver and argument are swapped
        CtInvocation<?> newInvocation =
                getFactory()
                        .Code()
                        .createInvocation(
                                element.getArguments().get(0),
                                ctExecutableReferenceToMethodToBeCalled,
                                element.getTarget());

        // Replace the old invocation by the new one
        element.replace(newInvocation);

        // Delete the null check on the variable if it exists
        deleteNullCheckIfExists(newInvocation);
    }

    private void deleteNullCheckIfExists(CtInvocation<?> newInvocation) {
        // the following is to handle the case in which there is a null check on the variable
        // used as target
        CtExpression<?> variable = newInvocation.getArguments().get(0);
        CtElement parent = newInvocation.getParent();
        if (parent instanceof CtBinaryOperator) {
            CtBinaryOperator<?> parentBinaryOperator = (CtBinaryOperator<?>) parent;
            CtElement parentLeftHandOperand = parentBinaryOperator.getLeftHandOperand();
            if (parentLeftHandOperand instanceof CtBinaryOperator
                    && isNullCheckOnTheVariable(
                            (CtBinaryOperator) parentLeftHandOperand, variable)) {
                parentBinaryOperator.replace(newInvocation);
            }
        }
    }

    private boolean isNullCheckOnTheVariable(
            CtBinaryOperator ctBinaryOperator, CtExpression<?> variable) {
        return ctBinaryOperator.getKind().equals(BinaryOperatorKind.NE)
                && ((ctBinaryOperator.getLeftHandOperand().equals(variable)
                                && ctBinaryOperator.getRightHandOperand().toString().equals("null"))
                        || (ctBinaryOperator.getLeftHandOperand().toString().equals("null")
                                && ctBinaryOperator.getRightHandOperand().equals(variable)));
    }
}
