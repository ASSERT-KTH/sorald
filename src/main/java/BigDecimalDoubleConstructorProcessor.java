import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.reference.CtTypeReference;
import java.util.List;
import java.math.BigDecimal;

public class BigDecimalDoubleConstructorProcessor extends AbstractProcessor<CtConstructorCall> {

    public BigDecimalDoubleConstructorProcessor(String projectKey) throws Exception {
        ParseAPI.parse(2111,"",projectKey);
    }

    @Override
    public boolean isToBeProcessed(CtConstructorCall cons)
    {
        //System.out.println(cons);
        //System.out.println(cons.getType());
        // cons.getType() == CtTypeReference
        CtTypeReference bigDecimalTypeRef = getFactory().createCtTypeReference(BigDecimal.class);
        CtTypeReference doubleTypeRef = getFactory().createCtTypeReference(double.class);


        if(cons.getType().equals(bigDecimalTypeRef)){
            List<CtExpression> expr = cons.getArguments();
            if(expr.size() == 1 && expr.get(0).getType().equals(doubleTypeRef)){
                return true;
            }
        }
        return false;
    }
    @Override
    public void process(CtConstructorCall cons) {
        System.out.println("Processing!");
        System.out.println(cons);
    }
}