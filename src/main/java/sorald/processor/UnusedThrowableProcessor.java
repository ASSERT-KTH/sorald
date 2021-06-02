package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtConstructorCall;

@ProcessorAnnotation(
        key = 3984,
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
