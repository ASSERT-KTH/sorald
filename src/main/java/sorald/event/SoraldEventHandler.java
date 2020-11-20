package sorald.event;

/** An event handler for reacting to different events in Sorald */
@FunctionalInterface
public interface SoraldEventHandler {
    void registerEvent(SoraldEvent event);
}
