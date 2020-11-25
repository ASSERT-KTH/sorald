package sorald.event;

import java.util.Collection;

/** Helper methods for firing off events. */
public class EventHelper {

    /**
     * Register an event with the given type with all handlers.
     *
     * @param type The event type
     * @param handlers The handlers to operate on
     */
    public static void fireEvent(
            EventType type, Collection<? extends SoraldEventHandler> handlers) {
        SoraldEvent event = () -> type;
        handlers.forEach(handler -> handler.registerEvent(event));
    }

    /**
     * Register the specified event with all handlers.
     *
     * @param event An event
     * @param handlers The handlers to operate on
     */
    public static void fireEvent(
            SoraldEvent event, Collection<? extends SoraldEventHandler> handlers) {
        handlers.forEach(handler -> handler.registerEvent(event));
    }
}
