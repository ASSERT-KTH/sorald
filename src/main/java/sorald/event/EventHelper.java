package sorald.event;

import java.util.Collection;

/** Helper methods for firing off events. */
public class EventHelper {

    /**
     * Fire off an event without any metadata for the given handlers.
     *
     * @param type The event type
     * @param handlers The handlers to operate on
     */
    public static void fireEvent(
            EventType type, Collection<? extends SoraldEventHandler> handlers) {
        handlers.forEach(handler -> handler.registerEvent(type));
    }

    /**
     * Fire off an event with metadata for the given handlers.
     *
     * @param type The event type
     * @param metadata Metadata for the event
     * @param handlers The handlers to operate on
     */
    public static void fireEvent(
            EventType type,
            EventMetadata metadata,
            Collection<? extends SoraldEventHandler> handlers) {
        handlers.forEach(handler -> handler.registerEvent(type, metadata));
    }
}
