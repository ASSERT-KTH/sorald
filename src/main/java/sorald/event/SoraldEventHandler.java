package sorald.event;

/** An event handler for reacting to different events in Sorald */
public interface SoraldEventHandler {

    /**
     * Register an event that does not have any accompanying metadata.
     *
     * @param type The type of the event
     */
    void registerEvent(EventType type);

    /**
     * Register an event that has accompanying metadata.
     *
     * @param type The type of the event
     * @param metadata Metadata related to the event
     */
    void registerEvent(EventType type, EventMetadata metadata);
}
