package sorald.qodana.processors;

import sorald.processor.SoraldAbstractProcessor;
import spoon.reflect.declaration.CtElement;

public abstract class AbstractQodanaProcessor<E extends CtElement>
        extends SoraldAbstractProcessor<E> {

    /**
     * Returns the name of the qodana as written in the qodana.yml file.
     *
     * @return the ruleId
     */
    public abstract String getRuleId();
}
