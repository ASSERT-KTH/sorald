package sorald;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;
import sorald.processor.SoraldAbstractProcessor;

public class Processors {

    private static final Map<Integer, Class<? extends SoraldAbstractProcessor>>
            RULE_KEY_TO_PROCESSOR = init();

    private static Map init() {
        Map<Integer, Class<? extends SoraldAbstractProcessor>> TEMP_RULE_KEY_TO_PROCESSOR =
                new HashMap<>();
        Reflections reflections = new Reflections(Constants.PROCESSOR_PACKAGE);
        Set<Class<? extends SoraldAbstractProcessor>> allProcessors =
                reflections.getSubTypesOf(SoraldAbstractProcessor.class);
        for (Class<? extends SoraldAbstractProcessor> processor : allProcessors) {
            Annotation[] annotations = processor.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof ProcessorAnnotation) {
                    ProcessorAnnotation myAnnotation = (ProcessorAnnotation) annotation;
                    TEMP_RULE_KEY_TO_PROCESSOR.putIfAbsent(myAnnotation.key(), processor);
                }
            }
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
        for (Map.Entry ruleKeyToProcessor : RULE_KEY_TO_PROCESSOR.entrySet()) {
            Class processorClass = (Class) ruleKeyToProcessor.getValue();
            Annotation[] annotations = processorClass.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof ProcessorAnnotation) {
                    ProcessorAnnotation myAnnotation = (ProcessorAnnotation) annotation;
                    descriptions +=
                            "\n" + ruleKeyToProcessor.getKey() + ": " + myAnnotation.description();
                }
            }
        }
        return descriptions;
    }
}
