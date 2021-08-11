package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtReturn;

// @spotless:off
/**
 * Returning `Integer.MIN_VALUE` can cause errors because the return value of `compareTo` is sometimes inversed, with the expectation that negative values become positive. However, inversing `Integer.MIN_VALUE` yields `Integer.MIN_VALUE` rather than `Integer.MAX_VALUE`. Any `return Integer.MIN_VALUE` in a `compareTo` method is then replaced by `return -1`.
 *
 * ```diff
 * -  public int compareTo(CompareToReturnValue a) {
 * -    return Integer.MIN_VALUE; // Noncompliant
 * -  }
 * +  public int compareTo(CompareToReturnValue a) {
 * +     return -1;
 * +  }
 * ```
 */
// @spotless:on
@ProcessorAnnotation(
        key = "S2167",
        description = "\"compareTo\" should not return \"Integer.MIN_VALUE\"")
public class CompareToReturnValueProcessor extends SoraldAbstractProcessor<CtReturn<?>> {

    @Override
    protected void repairInternal(CtReturn<?> ctReturn) {
        CtLiteral<?> elem2Replace = ctReturn.getFactory().createLiteral(-1);
        ctReturn.getReturnedExpression().replace(elem2Replace);
    }
}
