package sorald.processor;

import sorald.annotations.ProcessorAnnotation;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

@ProcessorAnnotation(
        key = "S2097",
        description = "\"equals(Object obj)\" should test argument type")
public class EqualsArgumentTypeProcessor extends SoraldAbstractProcessor<CtMethod<?>> {

    @Override
    protected void repairInternal(CtMethod<?> element) {
        String paramName = element.getParameters().get(0).getSimpleName();
        Factory factory = element.getFactory();

        CtExpression<Boolean> condition =
                factory.createCodeSnippetExpression(
                        String.format(
                                "%s == null || getClass() != %s.getClass()", paramName, paramName));
        CtIf ctIf = factory.createIf();
        ctIf.setCondition(condition);
        CtReturn<Boolean> ret = factory.createReturn();
        ret.setReturnedExpression(factory.createLiteral(false));
        ctIf.setThenStatement(factory.createCtBlock(ret));

        element.getBody().addStatement(0, ctIf);
    }
}
