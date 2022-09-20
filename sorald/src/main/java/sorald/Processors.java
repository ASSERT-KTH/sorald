package sorald;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import sorald.api.ProcessorRepository;
import sorald.processor.SoraldAbstractProcessor;

public class Processors {
    private Processors() {}

    public static Class<? extends SoraldAbstractProcessor<?>> getProcessor(String key) {
        ServiceLoader<ProcessorRepository> loader = ServiceLoader.load(ProcessorRepository.class);
        return loader.stream()
                .map(supplier -> supplier.get().getProcessor(key))
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
    }

    /**
     * @return A list of all processors sorted by name.
     */
    public static List<Class<? extends SoraldAbstractProcessor<?>>> getAllProcessors() {
        ServiceLoader<ProcessorRepository> loader = ServiceLoader.load(ProcessorRepository.class);
        return loader.stream()
                .map(supplier -> supplier.get().getAllProcessors())
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(Class::getSimpleName))
                .collect(Collectors.toList());
    }
}
