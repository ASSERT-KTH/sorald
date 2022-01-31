package sorald.processor;

import java.util.List;
import java.util.function.Supplier;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

@ProcessorAnnotation(key = "S4065", description = "\"ThreadLocal.withInitial\" should be preferred")
public class ThreadLocalWithInitial extends SoraldAbstractProcessor<CtNewClass<?>> {

    @Override
    protected void repairInternal(CtNewClass<?> newClass) {
        CtClass<?> innerClass = newClass.getAnonymousClass();
        CtExecutableReference<?> initialValueMethod = findInitialValueMethod(innerClass);
        CtLambda<?> lambda = createSupplier(initialValueMethod);
        CtInvocation<?> invocation = createInitialMethod(newClass, lambda);
        invocation.setArguments(List.of(lambda));
        newClass.replace(invocation);
    }

    private CtInvocation<?> createInitialMethod(CtNewClass<?> threadLocal, CtLambda<?> lambda) {
        CtTypeAccess<Object> target = getFactory().createTypeAccess(createThreadLocalRef());
        CtExecutableReference<?> executableReference =
                getFactory()
                        .Executable()
                        .createReference(
                                threadLocal.getType(),
                                true,
                                threadLocal.getType(),
                                "withInitial",
                                List.of(lambda.getType()));
        return getFactory().createInvocation(target, executableReference);
    }

    private CtTypeReference<Object> createThreadLocalRef() {
        return getFactory().createCtTypeReference(ThreadLocal.class);
    }

    private CtLambda<?> createSupplier(CtExecutableReference<?> initialValueMethod) {
        CtLambda<?> lambda = getFactory().createLambda();
        if (initialValueMethod.getDeclaration().getBody().getStatements().size() == 1) {
            lambda.setExpression(
                    getReturnStatement(
                            initialValueMethod.getDeclaration().getBody().getStatement(0)));
        } else {
            lambda.setBody(initialValueMethod.getDeclaration().getBody());
        }
        lambda.setType(getFactory().createCtTypeReference(Supplier.class));
        return lambda;
    }

    private <T> CtExpression<T> getReturnStatement(CtStatement statement) {
        return ((CtReturn<T>) statement).getReturnedExpression();
    }

    private CtExecutableReference<?> findInitialValueMethod(CtClass<?> innerClass) {
        return innerClass.getDeclaredExecutables().stream()
                .filter(v -> v.getSimpleName().equals("initialValue"))
                .findFirst()
                .get();
    }
}
