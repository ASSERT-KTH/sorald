package sorald.processor;

import java.util.List;
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
        removeAllUnusedPrivateFieldWrites(element);
        element.delete();
    }

    /** Removes all writes to the given field that are not read anywhere. */
    private static void removeAllUnusedPrivateFieldWrites(CtField<?> field) {
        CtClass<?> parentClass = field.getParent(CtClass.class);
        List<CtFieldWrite<?>> unusedFieldWrites =
                parentClass.filterChildren(new TypeFilter<>(CtFieldWrite.class)).list();
        for (CtFieldWrite<?> unusedFieldWrite : unusedFieldWrites) {
            CtStatement statement = unusedFieldWrite.getParent(CtStatement.class);
            statement.delete();
        }
    }
}
