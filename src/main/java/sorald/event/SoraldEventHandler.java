package sorald.event;

public interface SoraldEventHandler {
    void registerEvent(EventType type);

    void registerEvent(EventType type, EventMetadata metadata);
}
