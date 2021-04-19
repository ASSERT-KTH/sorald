package sorald.processor;

import java.util.List;
import java.util.Set;
import sorald.annotations.IncompleteProcessor;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.ModifierKind;

@IncompleteProcessor(description = "Only handles implicit public constructor")
@ProcessorAnnotation(
        key = 1118,
        description = "Utility classes should not have public constructors")
public class UtilityClassWithPublicConstructorProcessor
        extends SoraldAbstractProcessor<CtClass<?>> {

    @Override
    protected boolean canRepairInternal(CtClass<?> candidate) {
        return candidate.getConstructors().stream().allMatch(CtElement::isImplicit);
    }

    @Override
    protected void repairInternal(CtClass<?> element) {
        addExplicitConstructor(element);
    }

    @SuppressWarnings("unchecked")
    private static <T> void addExplicitConstructor(CtClass<T> cls) {
        CtConstructor<T> explicitConstructor =
                cls.getFactory()
                        .createConstructor(
                                cls,
                                Set.of(ModifierKind.PRIVATE),
                                List.of(),
                                Set.of(),
                                cls.getFactory().createBlock());
        cls.getConstructor().replace(explicitConstructor);
    }
}
