import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;

public class NoSuchElementProcessor extends AbstractProcessor<CtElement> {
    private CtExpression denom;

    public NoSuchElementProcessor(String projectKey) throws Exception {
        ParseAPI.parse(2272,"",projectKey);
    }

    @Override
    public boolean isToBeProcessed(CtElement candidate) {
        boolean binary = candidate instanceof CtBinaryOperator;
        if (binary){
            CtBinaryOperator op = (CtBinaryOperator)candidate;
            if(op.getKind() == BinaryOperatorKind.DIV){
                denom = op.getRightHandOperand();
                return true;
            }
        }
        return false;
    }

    @Override
    public void process(CtElement candidate) {
        CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
        if (isToBeProcessed (candidate)) {
            CtStatement st = (CtStatement)candidate.getParent();
            CtExecutable method = (CtExecutable)(candidate.getParent().getParent().getParent());
            final String tryString = String.format(
                    "if(" + denom + " == 0) throw new ArithmeticException(\"Attempted division by zero in method %s\")",
                    method.getSimpleName());
            snippet.setValue(tryString);
            st.insertBefore(snippet);
        }
    }
}