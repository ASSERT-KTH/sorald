package sorald;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;
import sorald.processor.SoraldAbstractProcessor;
import sorald.sonar.IncompleteProcessor;

public class Processors {

    private static final Map<Integer, Class<? extends SoraldAbstractProcessor>>
            RULE_KEY_TO_PROCESSOR = init();

    private static Map<Integer, Class<? extends SoraldAbstractProcessor>> init() {
        Map<Integer, Class<? extends SoraldAbstractProcessor>> TEMP_RULE_KEY_TO_PROCESSOR =
                new HashMap<>();
        Reflections reflections = new Reflections(Constants.PROCESSOR_PACKAGE);
        Set<Class<? extends SoraldAbstractProcessor>> allProcessors =
                reflections.getSubTypesOf(SoraldAbstractProcessor.class);
        for (Class<? extends SoraldAbstractProcessor> processor : allProcessors) {
            ProcessorAnnotation annotation = processor.getAnnotation(ProcessorAnnotation.class);
            TEMP_RULE_KEY_TO_PROCESSOR.putIfAbsent(annotation.key(), processor);
        }
        return TEMP_RULE_KEY_TO_PROCESSOR;
    }

    public static Class<?> getProcessor(int ruleKey) {
        if (RULE_KEY_TO_PROCESSOR.containsKey(ruleKey)) {
            return RULE_KEY_TO_PROCESSOR.get(ruleKey);
        }
        return null;
    }

    public static String getRuleDescriptions() {
        String descriptions = "";
        for (Map.Entry<Integer, Class<? extends SoraldAbstractProcessor>> ruleKeyToProcessor :
                RULE_KEY_TO_PROCESSOR.entrySet()) {
            Class<? extends SoraldAbstractProcessor> processorClass = ruleKeyToProcessor.getValue();
            ProcessorAnnotation annotation =
                    processorClass.getAnnotation(ProcessorAnnotation.class);
            descriptions += "\n" + ruleKeyToProcessor.getKey() + ": " + annotation.description();

            IncompleteProcessor incomplete =
                    processorClass.getAnnotation(IncompleteProcessor.class);
            if (incomplete != null) {
                descriptions += "\n\t(incomplete: " + incomplete.description() + ")";
            }
        }
        return descriptions;
    }
}
