package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtConstructorCall;

/**
 * Throw a `Throwable` that has been created but not thrown.
 *
 * Example:
 * ```diff
 *         if (x < 0) {
 * -           new IllegalArgumentException("x must be nonnegative"); // Noncompliant {{Throw this exception or remove this useless statement}}
 * +           throw new IllegalArgumentException("x must be nonnegative");
 *         }
 * ```
 */
@ProcessorAnnotation(
        key = "S3984",
        description = "Exception should not be created without being thrown")
public class UnusedThrowableProcessor
        extends SoraldAbstractProcessor<CtConstructorCall<? extends Throwable>> {

    @Override
    protected void repairInternal(CtConstructorCall<? extends Throwable> element) {
        var ctThrow = getFactory().createThrow();
        var thrownExpr = element.clone();
        for (CtComment comment : thrownExpr.getComments()) {
            comment.delete();
            ctThrow.addComment(comment);
        }
        ctThrow.setThrownExpression(thrownExpr);
        element.replace(ctThrow);
    }
}
