package sorald.processor;

import java.util.NoSuchElementException;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

/**
 * Any implementation of the `Iterator.next()` method that does not throw `NoSuchElementException` has a code snippet added to its start. The code snippet consists of a call to `hasNext()` and a throw of the error.
 *
 * Example:
 * ```diff
 * +import java.util.NoSuchElementException;
 *
 * public class IteratorNextException implements Iterator {
 * ...
 *      @Override
 * -    public String next() { // Noncompliant
 * +    public String next() {
 * +        if (!hasNext()) {
 * +            throw new NoSuchElementException();
 * +        }
 *          ...
 *      }
 * ```
 *
 * Check out an accepted PR in [Apache PDFBox](https://github.com/apache/pdfbox/pull/75) that repairs two IteratorNextException violations.
 */
@ProcessorAnnotation(
        key = "S2272",
        description = "\"Iterator.next()\" methods should throw \"NoSuchElementException\"")
public class IteratorNextExceptionProcessor extends SoraldAbstractProcessor<CtMethod> {

    @Override
    protected void repairInternal(CtMethod method) {
        CtIf anIf = getFactory().Core().createIf();
        CtCodeSnippetExpression expr = getFactory().Core().createCodeSnippetExpression();
        expr.setValue("!hasNext()");
        anIf.setCondition(expr);
        CtType noSuchElementClass = getFactory().Class().get(NoSuchElementException.class);
        CtThrow throwStmnt = getFactory().createCtThrow("");
        throwStmnt.setThrownExpression(
                ((CtExpression<? extends Throwable>)
                        getFactory()
                                .createConstructorCall(
                                        noSuchElementClass.getReference(), new CtExpression[] {})));
        CtBlock block = getFactory().Core().createBlock();
        block.addStatement(throwStmnt);
        anIf.setThenStatement(block);
        method.getBody().getStatements().get(0).insertBefore(anIf);
    }
}
