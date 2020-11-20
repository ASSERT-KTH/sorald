package sorald.event;

/** An event occurring during execution of Sorald. */
@FunctionalInterface
public interface SoraldEvent {

    /** @return The type of this event */
    EventType type();
}
