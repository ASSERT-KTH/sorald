package sorald.processor;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

@ProcessorAnnotation(key = "S4065", description = "\"ThreadLocal.withInitial\" should be preferred")
public class ThreadLocalWithInitial extends SoraldAbstractProcessor<CtNewClass<?>> {

    private static final String THREADLOCAL_FQN = "java.lang.ThreadLocal";

    @Override
    protected void repairInternal(CtNewClass<?> newClass) {
        if (isThreadLocalType(newClass)) {
            CtClass<?> innerClass = newClass.getAnonymousClass();
            if (hasNoFields(innerClass) && hasOnlyConstructorAndSingleMethod(innerClass)) {
                Optional<CtExecutableReference<?>> initalValueMethod =
                        findInitalValueMethod(innerClass);
                if (initalValueMethod.isPresent()) {
                    CtLambda<?> lambda = createSupplier(initalValueMethod.get());
                    CtInvocation<?> invocation = createInitalMethod(newClass, lambda);
                    invocation.setArguments(List.of(lambda));
                    newClass.replace(invocation);
                }
            }
        }
    }

    private boolean isThreadLocalType(CtNewClass<?> newClass) {
        return newClass.getType() != null
                && newClass.getType().getQualifiedName().equals(THREADLOCAL_FQN);
    }

    private CtInvocation<?> createInitalMethod(CtNewClass<?> threadLocal, CtLambda<?> lambda) {
        return getFactory()
                .createInvocation(
                        getFactory().createTypeAccess(createThreadLocalRef()),
                        getFactory()
                                .Executable()
                                .createReference(
                                        threadLocal.getType(),
                                        true,
                                        threadLocal.getType(),
                                        "withInitial",
                                        List.of(lambda.getType())));
    }

    private CtTypeReference<Object> createThreadLocalRef() {
        return getFactory().createCtTypeReference(ThreadLocal.class);
    }

    private CtLambda<?> createSupplier(CtExecutableReference<?> initalValueMethod) {
        CtLambda<?> lambda = getFactory().createLambda();
        if (initalValueMethod.getDeclaration().getBody().getStatements().size() == 1) {
            CtStatement statement = initalValueMethod.getDeclaration().getBody().getStatement(0);
            if (statement instanceof CtReturn) {
                lambda.setExpression(getReturnStatement(statement));
            } else {
                lambda.setBody(initalValueMethod.getDeclaration().getBody());
            }
        } else {
            lambda.setBody(initalValueMethod.getDeclaration().getBody());
        }
        lambda.setType(getFactory().createCtTypeReference(Supplier.class));
        return lambda;
    }

    private <T> CtExpression<T> getReturnStatement(CtStatement statement) {
        return ((CtReturn<T>) statement).getReturnedExpression();
    }

    private Optional<CtExecutableReference<?>> findInitalValueMethod(CtClass<?> innerClass) {
        return innerClass.getDeclaredExecutables().stream()
                .filter(v -> v.getSimpleName().equals("initialValue"))
                .findFirst();
    }

    private boolean hasOnlyConstructorAndSingleMethod(CtClass<?> innerClass) {
        return innerClass.getDeclaredExecutables().size() == 2;
    }

    private boolean hasNoFields(CtClass<?> innerClass) {
        return innerClass.getDeclaredFields().isEmpty();
    }
}
