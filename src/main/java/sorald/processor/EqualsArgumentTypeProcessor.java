package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;

@ProcessorAnnotation(key = 2097, description = "\"equals(Object obj)\" should test argument type")
public class EqualsArgumentTypeProcessor extends SoraldAbstractProcessor<CtMethod<?>> {

    @Override
    protected void repairInternal(CtMethod<?> element) {
        String paramName = element.getParameters().get(0).getSimpleName();
        CtStatement typeTest =
                element.getFactory()
                        .createCodeSnippetStatement(
                                String.format(
                                        "if (%s == null || getClass() != %s.getClass()) { return false; }",
                                        paramName, paramName));
        element.getBody().addStatement(0, typeTest);
    }
}
