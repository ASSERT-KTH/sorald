package sorald.qodana.processors;

import com.google.auto.service.AutoService;
import java.util.List;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.visitor.filter.TypeFilter;

@AutoService(AbstractQodanaProcessor.class)
@ProcessorAnnotation(
        key = "StringOperationCanBeSimplified",
        description = "This processor tries to simplify string operations.")
public class StringOperationCanBeSimplifiedProcessor
        extends AbstractQodanaProcessor<CtInvocation<?>> {

    @Override
    public String getRuleId() {
        return "StringOperationCanBeSimplified";
    }

    @Override
    protected void repairInternal(CtInvocation<?> innvocation) {
        // Qodana doesn't give good positions, so we have to iterate a bit
        List<CtInvocation<?>> innvocations =
                innvocation.getElements(new TypeFilter<>(CtInvocation.class));
        for (CtInvocation<?> ctInvocation : innvocations) {
            if (ctInvocation.getExecutable() != null
                    && ctInvocation.getExecutable().getType() != null
                    && ctInvocation
                            .getExecutable()
                            .getType()
                            .isSubtypeOf(
                                    ctInvocation
                                            .getFactory()
                                            .Type()
                                            .get(CharSequence.class)
                                            .getReference())
                    && ctInvocation.getExecutable().getSimpleName().equals("toString")) {
                ctInvocation.replace(ctInvocation.getTarget());
            }
        }
    }
}
