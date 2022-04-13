package sorald.qodana;

import java.util.List;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.stream.Collectors;
import sorald.api.ProcessorRepository;
import sorald.processor.SoraldAbstractProcessor;
import sorald.qodana.processors.AbstractQodanaProcessor;

public class QodanaProcessorRepository implements ProcessorRepository {

    @Override
    public Class<? extends SoraldAbstractProcessor<?>> getProcessor(String id) {
        for (AbstractQodanaProcessor<?> processor :
                ServiceLoader.load(AbstractQodanaProcessor.class)) {
            if (processor.getRuleId().equals(id)) {
                return (Class<? extends SoraldAbstractProcessor<?>>) processor.getClass();
            }
        }
        return null;
    }

    @Override
    public List<Class<? extends SoraldAbstractProcessor<?>>> getAllProcessors() {
        return ServiceLoader.load(AbstractQodanaProcessor.class).stream()
                .map(Provider::get)
                .map(v -> (Class<? extends SoraldAbstractProcessor<?>>) v.getClass())
                .collect(Collectors.toList());
    }
}
