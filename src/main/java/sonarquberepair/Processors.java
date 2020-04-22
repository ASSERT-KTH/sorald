package sonarquberepair;

import sonarquberepair.processor.sonarbased.DeadStoreProcessor;
import sonarquberepair.processor.sonarbased.SerializableFieldInSerializableClassProcessor;
import sonarquberepair.processor.sonarbased.UnclosedResourcesProcessor;
import sonarquberepair.processor.spoonbased.ArrayHashCodeAndToStringProcessor;
import sonarquberepair.processor.spoonbased.BigDecimalDoubleConstructorProcessor;
import sonarquberepair.processor.spoonbased.CastArithmeticOperandProcessor;
import sonarquberepair.processor.spoonbased.CompareStringsBoxedTypesWithEqualsProcessor;
import sonarquberepair.processor.spoonbased.IteratorNextExceptionProcessor;
import sonarquberepair.processor.spoonbased.GetClassLoaderProcessor;
import sonarquberepair.processor.spoonbased.CompareToReturnValueProcessor;
import sonarquberepair.processor.spoonbased.MathOnFloatProcessor;

import spoon.processing.Processor;

import java.util.HashMap;
import java.util.Map;

import static java.lang.System.exit;

public class Processors {

	private static final Map<Integer, Class<? extends Processor>> RULE_KEY_TO_PROCESSOR = init();

	private static Map init() {
		Map<Integer, Class<? extends Processor>> TEMP_RULE_KEY_TO_PROCESSOR = new HashMap<>();
		TEMP_RULE_KEY_TO_PROCESSOR.putIfAbsent(1854, DeadStoreProcessor.class);
		TEMP_RULE_KEY_TO_PROCESSOR.putIfAbsent(1948, SerializableFieldInSerializableClassProcessor.class);
		TEMP_RULE_KEY_TO_PROCESSOR.putIfAbsent(2095, UnclosedResourcesProcessor.class);
		TEMP_RULE_KEY_TO_PROCESSOR.putIfAbsent(2111, BigDecimalDoubleConstructorProcessor.class);
		TEMP_RULE_KEY_TO_PROCESSOR.putIfAbsent(2116, ArrayHashCodeAndToStringProcessor.class);
		TEMP_RULE_KEY_TO_PROCESSOR.putIfAbsent(2272, IteratorNextExceptionProcessor.class);
		TEMP_RULE_KEY_TO_PROCESSOR.putIfAbsent(4973, CompareStringsBoxedTypesWithEqualsProcessor.class);
		TEMP_RULE_KEY_TO_PROCESSOR.putIfAbsent(2184, CastArithmeticOperandProcessor.class);
		TEMP_RULE_KEY_TO_PROCESSOR.putIfAbsent(3032, GetClassLoaderProcessor.class);
		TEMP_RULE_KEY_TO_PROCESSOR.putIfAbsent(2167, CompareToReturnValueProcessor.class);
		TEMP_RULE_KEY_TO_PROCESSOR.putIfAbsent(2164, MathOnFloatProcessor.class);
		return TEMP_RULE_KEY_TO_PROCESSOR;
	}

	public static Class<?> getProcessor(int ruleKey) {
		if (!RULE_KEY_TO_PROCESSOR.containsKey(ruleKey)) {
			System.out.println("Sorry, repair not available for rule " + ruleKey);
			exit(0);
		}
		return RULE_KEY_TO_PROCESSOR.get(ruleKey);
	}

}
