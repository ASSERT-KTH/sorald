package sorald.processor;

import java.util.Collections;
import java.util.Locale;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;

@ProcessorAnnotation(
        key = "S1596",
        description =
                "\"Collections.EMPTY_LIST\", \"EMPTY_MAP\", and \"EMPTY_SET\" should not be used")
public class CollectionsEmptyConstantsProcessor extends SoraldAbstractProcessor<CtFieldAccess<?>> {

    @Override
    protected void repairInternal(CtFieldAccess<?> element) {
        String[] loweredNameParts = element.getVariable().getSimpleName().toLowerCase().split("_");
        String camelCasedName = loweredNameParts[0] + capitalize(loweredNameParts[1]);
        CtMethod<?> methodToBeCalled =
                getFactory().Class().get(Collections.class).getMethod(camelCasedName);

        CtInvocation<?> newInvocation =
                getFactory().createInvocation(element.getTarget(), methodToBeCalled.getReference());

        element.replace(newInvocation);
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase(Locale.ENGLISH) + s.substring(1);
    }
}
