import spoon.processing.Processor;

import java.util.HashMap;
import java.util.Map;

import static java.lang.System.exit;

public class Processors {

    private static Map<Integer, Class<? extends Processor>> RULE_KEY_TO_PROCESSOR;

    public static void init() {
        RULE_KEY_TO_PROCESSOR = new HashMap<>();
        RULE_KEY_TO_PROCESSOR.putIfAbsent(1854, DeadStoreProcessor.class);
        RULE_KEY_TO_PROCESSOR.putIfAbsent(1948, SerializableFieldInSerializableClassProcessor.class);
        RULE_KEY_TO_PROCESSOR.putIfAbsent(2095, UnclosedResourcesProcessor.class);
        RULE_KEY_TO_PROCESSOR.putIfAbsent(2111, BigDecimalDoubleConstructorProcessor.class);
        RULE_KEY_TO_PROCESSOR.putIfAbsent(2116, ArrayHashCodeAndToStringProcessor.class);
        RULE_KEY_TO_PROCESSOR.putIfAbsent(2272, IteratorNextExceptionProcessor.class);
        RULE_KEY_TO_PROCESSOR.putIfAbsent(4973, CompareStringsBoxedTypesWithEqualsProcessor.class);
    }

    public static Class<?> getProcessor(int ruleKey) {
        if (RULE_KEY_TO_PROCESSOR == null) {
            init();
        }
        if (!RULE_KEY_TO_PROCESSOR.containsKey(ruleKey)) {
            System.out.println("Sorry, repair not available for rule " + ruleKey);
            exit(0);
        }
        return RULE_KEY_TO_PROCESSOR.get(ruleKey);
    }

}
