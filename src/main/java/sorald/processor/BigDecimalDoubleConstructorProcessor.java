package sorald.processor;

import java.math.BigDecimal;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;

// @spotless:off
/**
 * Any constructor of `BigDecimal` that has a parameter of type `float` or `double` is replaced with an invocation of the `BigDecimal.valueOf(parameter)` method.
 *
 * Example:
 * ```diff
 *          double d = 1.1;
 *          float f = 2.2;
 * -        BigDecimal bd1 = new BigDecimal(d);// Noncompliant
 * -        BigDecimal bd2 = new BigDecimal(1.1); // Noncompliant
 * -        BigDecimal bd3 = new BigDecimal(f); // Noncompliant
 * +        BigDecimal bd1 = BigDecimal.valueOf(d);
 * +        BigDecimal bd2 = BigDecimal.valueOf(1.1);
 * +        BigDecimal bd3 = BigDecimal.valueOf(f);
 * ```
 *
 * When the constructor of `BigDecimal` being called has two arguments, being the first one of type `float` or `double`, that argument is changed to `String`.
 *
 * Example:
 * ```diff
 *         MathContext mc;
 * -       BigDecimal bd4 = new BigDecimal(2.0, mc); // Noncompliant {{Use "BigDecimal.valueOf" instead.}}
 * -       BigDecimal bd6 = new BigDecimal(2.0f, mc); // Noncompliant {{Use "BigDecimal.valueOf" instead.}}
 * +       BigDecimal bd4 = new BigDecimal("2.0", mc);
 * +       BigDecimal bd6 = new BigDecimal("2.0", mc);
 * ```
 *
 * Check out an accepted PR in [Apache PDFBox](https://github.com/apache/pdfbox/pull/76) that repairs one BigDecimalDoubleConstructor violation.
 */
// @spotless:on
@ProcessorAnnotation(key = "S2111", description = "\"BigDecimal(double)\" should not be used")
public class BigDecimalDoubleConstructorProcessor
        extends SoraldAbstractProcessor<CtConstructorCall> {

    @Override
    protected void repairInternal(CtConstructorCall cons) {
        if (cons.getArguments().size() == 1) {
            CtType bigDecimalClass = getFactory().Class().get(BigDecimal.class);
            CtCodeSnippetExpression invoker =
                    getFactory().Code().createCodeSnippetExpression("BigDecimal");
            CtMethod valueOfMethod = (CtMethod) bigDecimalClass.getMethodsByName("valueOf").get(0);
            CtExecutableReference refToMethod =
                    getFactory().Executable().createReference(valueOfMethod);
            CtExpression arg = (CtExpression) cons.getArguments().get(0);
            CtInvocation newInvocation =
                    getFactory().Code().createInvocation(invoker, refToMethod, arg);
            cons.replace(newInvocation);
        } else {
            CtConstructorCall newCtConstructorCall = cons.clone();
            CtExpression arg = (CtExpression) cons.getArguments().get(0);
            String argValue = arg.toString().replaceAll("[fFdD]", "");
            CtLiteral<String> literal = getFactory().Code().createLiteral(argValue);
            newCtConstructorCall.getArguments().set(0, literal);
            cons.replace(newCtConstructorCall);
        }
    }
}
