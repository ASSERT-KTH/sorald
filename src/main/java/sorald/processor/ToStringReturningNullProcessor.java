package sorald.processor;

import sorald.Constants;
import sorald.annotations.IncompleteProcessor;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtMethod;

// @spotless:off
/**
 * For the return statements inside "toString()", this processor replaces the return expression with an empty string.
 *
 * Note that this processor is incomplete and does not fix null-returning `clone()` methods.
 *
 * Example:
 * ```diff
 * public String toString() {
 *    Random r = new Random();
 *    if(r.nextInt(10) == r.nextInt(10)){
 * -     return null; // Noncompliant
 * +     return ""; // Noncompliant
 *    }
 *    else if(r.nextInt(10) == r.nextInt(10)){
 *       return "null";
 *    }
 *    return "";
 * }
 * ```
 */
// @spotless:on
@IncompleteProcessor(description = "does not fix null returning clone()")
@ProcessorAnnotation(
        key = "S2225",
        description = "\"toString()\" and \"clone()\" methods should not return null")
public class ToStringReturningNullProcessor extends SoraldAbstractProcessor<CtReturn<?>> {

    @Override
    protected boolean canRepairInternal(CtReturn<?> candidate) {
        CtMethod<?> parentMethod = candidate.getParent(CtMethod.class);
        return parentMethod.getSignature().equals(Constants.TOSTRING_METHOD_NAME + "()")
                && candidate.getReturnedExpression().toString().equals("null");
    }

    @Override
    protected void repairInternal(CtReturn<?> element) {
        element.getReturnedExpression().replace(getFactory().Code().createLiteral(""));
    }
}
