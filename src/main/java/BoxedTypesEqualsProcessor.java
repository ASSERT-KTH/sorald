import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.Arrays;

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
            if(op.getKind() == BinaryOperatorKind.EQ){
                CtExpression left = op.getLeftHandOperand();
                CtExpression right = op.getRightHandOperand();
                /*
                Boxing boxed types returns the same type.
                The reason we don't check for the case where one variable is boxed is because Java implicitly unboxes
                the boxed type to primitive, making the == check fine. See JLS #5.6.2:
                https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.6.2
                 */
                if(left.getType().box() == left.getType() && right.getType().box() == right.getType()){
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public void process(CtElement element) {
        CtBinaryOperator binaryOperator = (CtBinaryOperator)element;
        // System.out.println(binaryOperator.getLeftHandOperand().getType().getTypeDeclaration().getMethodsByName("equals").get(0)); // String
        // System.out.println(binaryOperator.getLeftHandOperand().getType().getClass()); // class spoon.support.reflect.reference.CtTypeReferenceImpl
        // CtType leftClass = getFactory().Type().STRING.getTypeDeclaration();
        // CtMethod leftMethod = (CtMethod) binaryOperator.getLeftHandOperand().getType().getTypeDeclaration().getMethodsByName("equals").get(0);
        CtCodeSnippetExpression newBinaryOperator = getFactory().Code().createCodeSnippetExpression(binaryOperator.getLeftHandOperand().toString() + ".equals(" + binaryOperator.getRightHandOperand() + ");");
        // System.out.println(newBinaryOperator);
        binaryOperator.replace(newBinaryOperator);
        // CtType leftClass = getFactory().Class().get(binaryOperator.getLeftHandOperand().getType().class);
        // CtMethod method = null;
        // method = (CtMethod) leftClass.getMethodsByName("equals").get(0);
        // System.out.println(leftClass);
        // System.out.println(leftMethod);
    }
}