package sorald;

import java.util.HashMap;
import java.util.Map;
import sorald.processor.*;

public class Processors {
    private static final Map<Integer, Class<? extends SoraldAbstractProcessor<?>>>
            RULE_KEY_TO_PROCESSOR =
                    new HashMap<>() {
                        {
                        }
                    };

    public static final String RULE_DESCRIPTIONS = "";

    public static Class<? extends SoraldAbstractProcessor<?>> getProcessor(int key) {
        return RULE_KEY_TO_PROCESSOR.get(key);
    }
}
