package sorald.processor;

import sorald.ProcessorAnnotation;
import spoon.reflect.code.CtInvocation;

@ProcessorAnnotation(key = 2755, description = "XML parsers should not be vulnerable to XXE attacks")
public class XxeProcessingProcessor extends SoraldAbstractProcessor<CtInvocation<?>> {
    @Override
    public boolean isToBeProcessed(CtInvocation<?> candidate) {
        return super.isToBeProcessedAccordingToStandards(candidate);
    }

    @Override
    public void process(CtInvocation<?> element) {
        super.process(element);
    }
}
