package sorald.processor;

import sorald.Constants;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtMethod;

@ProcessorAnnotation(
        key = 2167,
        description = "\"compareTo\" should not return \"Integer.MIN_VALUE\"")
public class CompareToReturnValueProcessor extends SoraldAbstractProcessor<CtReturn<?>> {

    @Override
    protected boolean canRepairInternal(CtReturn<?> ctReturn) {
        CtMethod ctMethod = ctReturn.getParent(CtMethod.class);
        String returnTypeName = ctMethod.getType().getSimpleName();
        if (ctMethod.getSimpleName().equals("compareTo")
                && (returnTypeName.equals(Constants.INT) || returnTypeName.equals("Integer"))
                && ctReturn.getReturnedExpression().toString().indexOf("Integer.MIN_VALUE") != -1) {
            return true;
        }
        return false;
    }

    @Override
    protected void repairInternal(CtReturn<?> ctReturn) {
        CtLiteral<?> elem2Replace = ctReturn.getFactory().createLiteral(-1);
        ctReturn.getReturnedExpression().replace(elem2Replace);
    }
}
