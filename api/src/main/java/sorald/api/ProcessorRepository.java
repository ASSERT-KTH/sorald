package sorald.api;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This interfaces describes a repository of processors. Each processor is a class that implements
 * the {@link SoraldAbstractProcessor}. Each processor is identified by an unique ID and must have a
 * public constructor without args. The ID is used to identify the processor in the code.
 */
public interface ProcessorRepository {
    /**
     * Returns a processor for the given id.
     *
     * @param id The id of the processor.
     * @return The processor class for the given id or null if no processor is found.
     */
    @Nullable
    Class<? extends SoraldAbstractProcessor<?>> getProcessor(@Nullable String id);

    /**
     * Returns a list of all available processors.
     *
     * @return A list of all available processors.
     */
    @Nonnull
    List<Class<? extends SoraldAbstractProcessor<?>>> getAllProcessors();
}
