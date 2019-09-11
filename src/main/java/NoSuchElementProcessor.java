import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import java.util.NoSuchElementException;

public class NoSuchElementProcessor extends AbstractProcessor<CtMethod> {
    public NoSuchElementProcessor(String projectKey) throws Exception {
        ParseAPI.parse(2272,"",projectKey);
    }

    @Override
    public boolean isToBeProcessed(CtMethod candidate) {
        if(candidate.getParent(CtClass.class).getSuperInterfaces().toString().contains("Iterator")){
            if(candidate.getSignature().contains("next()") &&
                    !candidate.getBody().getStatements().toString().contains("throw new NoSuchElementException")){
                return true;
            }
        }
        return false;
    }

    @Override
    public void process(CtMethod method) {
        CtIf anIf = getFactory().Core().createIf();
        CtCodeSnippetExpression expr = getFactory().Core().createCodeSnippetExpression();
        expr.setValue("!hasNext()");
        anIf.setCondition(expr);
        CtType noSuchElementClass = getFactory().Class().get(NoSuchElementException.class);
        CtCodeSnippetStatement stmnt = getFactory().Core().createCodeSnippetStatement();
        stmnt.setValue("throw new " + ((CtClass) noSuchElementClass).getConstructor().getSignature());
        CtBlock block = getFactory().Core().createBlock();
        block.addStatement(stmnt);
        anIf.setThenStatement(block);
        method.getBody().getStatements().get(0).insertBefore(anIf);
    }
}