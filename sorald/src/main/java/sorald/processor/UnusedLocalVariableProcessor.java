package sorald.processor;

import sorald.annotations.ProcessorAnnotation;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.factory.Factory;

@ProcessorAnnotation(key = "S1481", description = "Unused local variables should be removed")
public class UnusedLocalVariableProcessor extends SoraldAbstractProcessor<CtLocalVariable<?>> {

    @Override
    protected void repairInternal(CtLocalVariable<?> element) {
        if (element.getParent() instanceof CtForEach) {
            transformIterableIntoForLoop(element);

        } else {
            element.delete();
        }
    }

    private static void transformIterableIntoForLoop(CtLocalVariable<?> element) {
        String newSimpleName = element.getSimpleName() + "Iterator";
        CtLoop loop = element.getParent(CtLoop.class);
        CtStatement body = loop.getBody();

        Factory factory = element.getFactory();
        CtFor conventionalForLoop = factory.createFor();
        conventionalForLoop.setBody(body);

        // int inputIterator = 0
        CtLocalVariable<Integer> forInit = factory.createLocalVariable();
        forInit.setSimpleName(newSimpleName);
        forInit.setType(factory.Type().INTEGER_PRIMITIVE);
        forInit.setAssignment(factory.createCodeSnippetExpression("0"));
        conventionalForLoop.addForInit(forInit);

        // ++inputIterator
        CtUnaryOperator<Integer> forUpdate = factory.createUnaryOperator();
        forUpdate.setKind(UnaryOperatorKind.PREINC);
        forUpdate.setOperand(factory.createCodeSnippetExpression(newSimpleName));
        conventionalForLoop.addForUpdate(forUpdate);

        CtExpression<Boolean> endCondition;
        CtExpression<?> forLoopIterable = element.getParent(CtForEach.class).getExpression();
        if (((CtForEach) element.getParent()).getExpression().getType().isArray()) {
            // inputIterator < input.length
            endCondition =
                    factory.createCodeSnippetExpression(
                            newSimpleName
                                    + " < "
                                    + ((CtVariableRead<?>) forLoopIterable)
                                            .getVariable()
                                            .getSimpleName()
                                    + ".length");
        } else {
            // inputIterator < input.size()
            endCondition =
                    factory.createCodeSnippetExpression(
                            newSimpleName
                                    + " < "
                                    + ((CtVariableRead<?>) forLoopIterable)
                                            .getVariable()
                                            .getSimpleName()
                                    + ".size()");
        }
        conventionalForLoop.setExpression(endCondition);
        element.getParent().replace(conventionalForLoop);
    }
}
