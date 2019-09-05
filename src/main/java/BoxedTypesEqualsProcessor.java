import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

public class BoxedTypesEqualsProcessor extends AbstractProcessor<CtElement> {

    public BoxedTypesEqualsProcessor(String projectKey) throws Exception {
        ParseAPI.parse(4973,"",projectKey);
    }

    @Override

    public boolean isToBeProcessed(CtElement candidate)
    {
        if(candidate==null)
        {
            return false;
        }
        if (candidate instanceof CtBinaryOperator){
            CtBinaryOperator op = (CtBinaryOperator)candidate;
            if(op.getKind() == BinaryOperatorKind.EQ || op.getKind() == BinaryOperatorKind.NE){
                CtExpression left = op.getLeftHandOperand();
                CtExpression right = op.getRightHandOperand();
                /*
                The reason we don't check for the case where one variable is boxed is because Java implicitly unboxes
                the boxed type to primitive, making the == check fine. See JLS #5.6.2:
                https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.6.2
                */
                CtTypeReference nullType = getFactory().Type().NULL_TYPE;
                if(!left.getType().isPrimitive() && !right.getType().isPrimitive() &&
                        !left.getType().isEnum() && !right.getType().isEnum() &&
                        !left.getType().equals(nullType) &&
                        !right.getType().equals(nullType)){
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public void process(CtElement element) {
        CtBinaryOperator bo = (CtBinaryOperator)element;
        String negation = "";
        if(((CtBinaryOperator) element).getKind() == BinaryOperatorKind.NE){
            negation = "!";
        }
        CtCodeSnippetExpression newBinaryOperator = getFactory().Code().createCodeSnippetExpression(
                negation + bo.getLeftHandOperand().toString() + ".equals(" + bo.getRightHandOperand() + ")");
        bo.replace(newBinaryOperator);
    }
}