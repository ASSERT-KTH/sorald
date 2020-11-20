package sorald.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EventMetadata {
    private final String id;
    private final Map<String, Object> rawMetadata;

    public EventMetadata(String id) {
        this.id = id;
        rawMetadata = new HashMap<>();
    }

    public EventMetadata put(String key, Object value) {
        rawMetadata.putIfAbsent(key, value);
        return this;
    }

    public Map<String, Object> getRawMetadata() {
        return Collections.unmodifiableMap(rawMetadata);
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "EventMetadata{" + "id='" + id + '\'' + ", rawMetadata=" + rawMetadata + '}';
    }
}
