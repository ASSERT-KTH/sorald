package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import sorald.api.SoraldAbstractProcessor;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtReturn;

@ProcessorAnnotation(
        key = "S2167",
        description = "\"compareTo\" should not return \"Integer.MIN_VALUE\"")
public class CompareToReturnValueProcessor extends SoraldAbstractProcessor<CtReturn<?>> {

    @Override
    protected void repairInternal(CtReturn<?> ctReturn) {
        CtLiteral<?> elem2Replace = ctReturn.getFactory().createLiteral(-1);
        ctReturn.getReturnedExpression().replace(elem2Replace);
    }
}
