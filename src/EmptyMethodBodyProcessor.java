
package fr.inria.gforge.spoon.analysis;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;

import java.util.ArrayList;
import java.util.List;

/**
 * Reports warnings when empty methods are found.
 */
public class EmptyMethodBodyProcessor extends AbstractProcessor<CtMethod<?>> {

    public final List<CtMethod> emptyMethods = new ArrayList<CtMethod>();
    public final List<CtMethod> allMethods = new ArrayList<CtMethod>();
    public void process(CtMethod<?> element) {
        allMethods.add(element);
        if (element.getParent(CtClass.class) != null && !element.getModifiers().contains(ModifierKind.ABSTRACT) && element.getBody().getStatements().size() == 0) {
                emptyMethods.add(element);
            CtStatement st=getFactory().Core().createReturn();
            CtExpression expr = getFactory().Core().createLiteral();
            ((CtLiteral) expr).setValue(5);
            ((CtReturn) st).setReturnedExpression(expr);
            element.setBody(st);
        }
    }
    @Override
    public void processingDone()
    {
        for(CtMethod x:allMethods)
        {
            //System.out.println(x);
            //System.out.println(x);
            System.out.println("\n\n\n\n\n");
        }
    }
}
