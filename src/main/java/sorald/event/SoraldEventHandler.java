package sorald.event;

/** An event handler for reacting to different events in Sorald */
public interface SoraldEventHandler {
    void registerEvent(SoraldEvent event);
}
