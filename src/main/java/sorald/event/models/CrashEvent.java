package sorald.event.models;

import java.io.PrintWriter;
import java.io.StringWriter;
import sorald.event.EventType;
import sorald.event.SoraldEvent;

/** Event recording a crash. */
public class CrashEvent implements SoraldEvent {
    private final String description;
    private final Exception exception;

    public CrashEvent(String description, Exception exception) {
        this.description = description;
        this.exception = exception;
        exception.printStackTrace();
    }

    @Override
    public EventType type() {
        return EventType.CRASH;
    }

    public String getStackTrace() {
        StringWriter buf = new StringWriter();
        exception.printStackTrace(new PrintWriter(buf));
        return buf.toString();
    }

    public String getMessage() {
        String excMsg = exception.getMessage();
        return excMsg.isEmpty() ? "N/A" : excMsg;
    }

    public String getDescription() {
        return description;
    }
}
