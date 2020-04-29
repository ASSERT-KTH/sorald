package sonarquberepair;

import sonarquberepair.processor.ArrayHashCodeAndToStringProcessor;
import sonarquberepair.processor.BigDecimalDoubleConstructorProcessor;
import sonarquberepair.processor.CastArithmeticOperandProcessor;
import sonarquberepair.processor.CompareStringsBoxedTypesWithEqualsProcessor;
import sonarquberepair.processor.CompareToReturnValueProcessor;
import sonarquberepair.processor.DeadStoreProcessor;
import sonarquberepair.processor.EqualsOnAtomicClassProcessor;
import sonarquberepair.processor.GetClassLoaderProcessor;
import sonarquberepair.processor.IteratorNextExceptionProcessor;
import sonarquberepair.processor.MathOnFloatProcessor;
import sonarquberepair.processor.SQRAbstractProcessor;
import sonarquberepair.processor.SerializableFieldInSerializableClassProcessor;
import sonarquberepair.processor.SynchronizationOnGetClassProcessor;
import sonarquberepair.processor.SynchronizationOnStringOrBoxedProcessor;
import sonarquberepair.processor.UnclosedResourcesProcessor;
import sonarquberepair.processor.UnusedThrowableProcessor;
import sonarquberepair.processor.SelfAssignementProcessor;

import java.util.HashMap;
import java.util.Map;

import static java.lang.System.exit;

public class Processors {

	private static final Map<Integer, Class<? extends SQRAbstractProcessor>> RULE_KEY_TO_PROCESSOR = init();

	private static Map init() {
		Map<Integer, Class<? extends SQRAbstractProcessor>> TEMP_RULE_KEY_TO_PROCESSOR = new HashMap<>();
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
		TEMP_RULE_KEY_TO_PROCESSOR.putIfAbsent(2204, EqualsOnAtomicClassProcessor.class);
		TEMP_RULE_KEY_TO_PROCESSOR.putIfAbsent(1860, SynchronizationOnStringOrBoxedProcessor.class);
		TEMP_RULE_KEY_TO_PROCESSOR.putIfAbsent(3067, SynchronizationOnGetClassProcessor.class);
		TEMP_RULE_KEY_TO_PROCESSOR.putIfAbsent(1656, SelfAssignementProcessor.class);
		TEMP_RULE_KEY_TO_PROCESSOR.putIfAbsent(3984, UnusedThrowableProcessor.class);
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
