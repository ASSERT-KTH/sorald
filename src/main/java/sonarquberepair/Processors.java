package sonarquberepair;

import java.lang.annotation.Annotation;
import java.util.Set;
import org.reflections.Reflections;
import sonarquberepair.processor.SQRAbstractProcessor;

import java.util.HashMap;
import java.util.Map;

import static java.lang.System.exit;

public class Processors {

	private static final Map<Integer, Class<? extends SQRAbstractProcessor>> RULE_KEY_TO_PROCESSOR = init();

	private static Map init() {
		Map<Integer, Class<? extends SQRAbstractProcessor>> TEMP_RULE_KEY_TO_PROCESSOR = new HashMap<>();
		Reflections reflections = new Reflections("sonarquberepair.processor");
		Set<Class<? extends SQRAbstractProcessor>> allProcessors = reflections.getSubTypesOf(SQRAbstractProcessor.class);
		for (Class<? extends SQRAbstractProcessor> processor : allProcessors) {
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
		if (!RULE_KEY_TO_PROCESSOR.containsKey(ruleKey)) {
			System.out.println("Sorry, repair not available for rule " + ruleKey);
			exit(0);
		}
		return RULE_KEY_TO_PROCESSOR.get(ruleKey);
	}

	public static String getRuleDescriptions() {
		String descriptions = "";
		for (Map.Entry ruleKeyToProcessor : RULE_KEY_TO_PROCESSOR.entrySet()) {
			Class processorClass = (Class) ruleKeyToProcessor.getValue();
			Annotation[] annotations = processorClass.getAnnotations();
			for (Annotation annotation : annotations) {
				if (annotation instanceof ProcessorAnnotation) {
					ProcessorAnnotation myAnnotation = (ProcessorAnnotation) annotation;
					descriptions += "\n" + ruleKeyToProcessor.getKey() + ": " + myAnnotation.description();
				}
			}
		}
		return descriptions;
	}

}
