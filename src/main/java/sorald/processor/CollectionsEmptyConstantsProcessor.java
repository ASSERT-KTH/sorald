package sorald.processor;

import java.util.Collections;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

@ProcessorAnnotation(
        key = 1596,
        description =
                "\"Collections.EMPTY_LIST\", \"EMPTY_MAP\", and \"EMPTY_SET\" should not be used")
public class CollectionsEmptyConstantsProcessor extends SoraldAbstractProcessor<CtFieldAccess<?>> {

    @Override
    protected void repairInternal(CtFieldAccess<?> element) {
        CtType<?> collectionsType = getFactory().Class().get(Collections.class);

        CtMethod<?> methodToBeCalled;
        if (element.getVariable().getSimpleName().equals("EMPTY_LIST")) {
            methodToBeCalled = collectionsType.getMethodsByName("emptyList").get(0);
        } else if (element.getVariable().getSimpleName().equals("EMPTY_MAP")) {
            methodToBeCalled = collectionsType.getMethodsByName("emptyMap").get(0);
        } else {
            methodToBeCalled = collectionsType.getMethodsByName("emptySet").get(0);
        }

        CtInvocation<?> newInvocation =
                getFactory().createInvocation(element.getTarget(), methodToBeCalled.getReference());

        element.replace(newInvocation);
    }
}
