package sorald.event;

import java.util.Map;

public interface SoraldEventHandler {
    void registerEvent(EventType type);

    void registerEvent(EventType type, EventMetadata metadata);

    void close();
}
