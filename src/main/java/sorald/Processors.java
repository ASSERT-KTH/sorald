package sorald;

import java.util.Map;
import sorald.processor.*;

/**
 * This class is partially generated. It is fine to edit non-generated code as per usual, but don't
 * change any of the generated fields unless you know precisely what you are doing.
 */
public class Processors {
    // GENERATED FIELD
    private static final Map<Integer, Class<? extends SoraldAbstractProcessor<?>>>
            RULE_KEY_TO_PROCESSOR =
                    new java.util.HashMap<>() {
                        {
                            put(1217, ThreadRunProcessor.class);
                            put(1444, PublicStaticFieldShouldBeFinalProcessor.class);
                            put(1656, SelfAssignementProcessor.class);
                            put(1854, DeadStoreProcessor.class);
                            put(1860, SynchronizationOnStringOrBoxedProcessor.class);
                            put(1948, SerializableFieldInSerializableClassProcessor.class);
                            put(2095, UnclosedResourcesProcessor.class);
                            put(2111, BigDecimalDoubleConstructorProcessor.class);
                            put(2116, ArrayHashCodeAndToStringProcessor.class);
                            put(2142, InterruptedExceptionProcessor.class);
                            put(2164, MathOnFloatProcessor.class);
                            put(2167, CompareToReturnValueProcessor.class);
                            put(2184, CastArithmeticOperandProcessor.class);
                            put(2204, EqualsOnAtomicClassProcessor.class);
                            put(2225, ToStringReturningNullProcessor.class);
                            put(2272, IteratorNextExceptionProcessor.class);
                            put(2755, XxeProcessingProcessor.class);
                            put(3032, GetClassLoaderProcessor.class);
                            put(3067, SynchronizationOnGetClassProcessor.class);
                            put(3984, UnusedThrowableProcessor.class);
                            put(4973, CompareStringsBoxedTypesWithEqualsProcessor.class);
                        }
                    };

    // GENERATED FIELD
    public static final String RULE_DESCRIPTIONS =
            "1217: \"Thread.run()\" should not be called directly\n1444: \"public static\" fields should be constant\n\t(incomplete: does not fix variable naming)\n1656: Variables should not be self-assigned\n1854: Unused assignments should be removed\n1860: Synchronization should not be based on Strings or boxed primitives\n1948: Fields in a \"Serializable\" class should either be transient or serializable\n2095: Resources should be closed\n2111: \"BigDecimal(double)\" should not be used\n2116: \"hashCode\" and \"toString\" should not be called on array instances\n2142: \"InterruptedException\" should not be ignored\n2164: Math should not be performed on floats\n2167: \"compareTo\" should not return \"Integer.MIN_VALUE\"\n2184: Math operands should be cast before assignment\n2204: \".equals()\" should not be used to test the values of \"Atomic\" classes\n2225: \"toString()\" and \"clone()\" methods should not return null\n\t(incomplete: does not fix null returning clone())\n2272: \"Iterator.next()\" methods should throw \"NoSuchElementException\"\n2755: XML parsers should not be vulnerable to XXE attacks\n\t(incomplete: This processor is a WIP and currently supports a subset of rule 2755. See Sorald\'s documentation for details.)\n3032: JEE applications should not \"getClassLoader\"\n3067: \"getClass\" should not be used for synchronization\n3984: Exception should not be created without being thrown\n4973: Strings and Boxed types should be compared using \"equals()\"";

    public static Class<? extends SoraldAbstractProcessor<?>> getProcessor(int key) {
        return RULE_KEY_TO_PROCESSOR.get(key);
    }
}
