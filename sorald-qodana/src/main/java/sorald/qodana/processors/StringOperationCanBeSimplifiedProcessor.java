package sorald.qodana.processors;

import com.google.auto.service.AutoService;
import spoon.reflect.code.CtInvocation;

@AutoService(AbstractQodanaProcessor.class)
public class StringOperationCanBeSimplifiedProcessor
        extends AbstractQodanaProcessor<CtInvocation<?>> {

    @Override
    public String getRuleId() {
        return "StringOperationCanBeSimplified";
    }

    @Override
    protected void repairInternal(CtInvocation<?> innvocation) {
        innvocation.replace(innvocation.getTarget());
        innvocation.getParent().replace(innvocation.getParent().clone());
    }
}
