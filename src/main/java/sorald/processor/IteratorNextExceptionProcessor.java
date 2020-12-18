package sorald.processor;

import java.util.Iterator;
import java.util.NoSuchElementException;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

@ProcessorAnnotation(
        key = 2272,
        description = "\"Iterator.next()\" methods should throw \"NoSuchElementException\"")
public class IteratorNextExceptionProcessor extends SoraldAbstractProcessor<CtMethod> {

    /**
     * @param candidate - Every method of the scanned file
     * @return Whether the method should have the transformation applied to it.
     *     <p>We want to process the next() method of any custom Iterator class which doesn't
     *     already throw the correct error.
     */
    @Override
    protected boolean canRepairInternal(CtMethod candidate) {
        CtType iteratorInterface = getFactory().Interface().get(Iterator.class);
        CtMethod next = (CtMethod) iteratorInterface.getMethodsByName("next").get(0);
        if (candidate.isOverriding(next)) {
            // If next() in the Iterator class is overridden, check if the correct error is thrown.
            Iterator<CtElement> statements = candidate.getBody().descendantIterator();
            CtTypeReference<?> noSuchElementTypeRef =
                    getFactory()
                            .Code()
                            .createCtTypeReference(java.util.NoSuchElementException.class);
            while (statements.hasNext()) {
                CtElement element = statements.next();
                // If a throw is found, check that it is the correct one
                if (element instanceof CtThrow) {
                    for (CtTypeReference typeRef : element.getReferencedTypes()) {
                        if (typeRef.equals(noSuchElementTypeRef)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

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
