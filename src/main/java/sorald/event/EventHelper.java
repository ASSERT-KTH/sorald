package sorald.event;

import java.util.Collection;

public class EventHelper {
    public static void fireEvent(Collection<? extends SoraldEventHandler> handlers, EventType type) {
        handlers.forEach(handler -> handler.registerEvent(type));
    }

    public static void fireEvent(Collection<? extends SoraldEventHandler> handlers, EventType type, EventMetadata metadata) {
        handlers.forEach(handler -> handler.registerEvent(type, metadata));
    }
}
