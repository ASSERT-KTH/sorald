package sorald.processor;

import java.util.List;
import java.util.stream.Collectors;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.visitor.filter.TypeFilter;

@ProcessorAnnotation(key = "S1068", description = "Unused \"private\" fields should be removed")
public class UnusedPrivateFieldProcessor extends SoraldAbstractProcessor<CtField<?>> {
    @Override
    protected void repairInternal(CtField<?> element) {
        List<CtFieldWrite> unusedFieldWrites = getFieldWritesOfSpecifiedField(element);

        for (CtFieldWrite<?> unusedFieldWrite : unusedFieldWrites) {
            CtStatement statement = unusedFieldWrite.getParent(CtStatement.class);
            statement.delete();
        }
        element.delete();
    }

    private static List<CtFieldWrite> getFieldWritesOfSpecifiedField(CtField<?> field) {
        CtClass<?> parentClass = field.getParent(CtClass.class);
        return parentClass
                .filterChildren(new TypeFilter<>(CtFieldWrite.class))
                .list(CtFieldWrite.class)
                .stream()
                .filter(fw -> fw.getVariable().getSimpleName().equals(field.getSimpleName()))
                .collect(Collectors.toList());
    }
}
