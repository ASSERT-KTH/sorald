package sorald;

import java.util.Map;
import sorald.processor.*;

public class Processors {
    private static final Map<Integer, Class<? extends SoraldAbstractProcessor<?>>>
            RULE_KEY_TO_PROCESSOR =
                    new java.util.HashMap<>() {
                        {
                            put(2272, IteratorNextExceptionProcessor.class);
                            put(1217, ThreadRunProcessor.class);
                            put(2755, XxeProcessingProcessor.class);
                            put(2116, ArrayHashCodeAndToStringProcessor.class);
                            put(1444, PublicStaticFieldShouldBeFinalProcessor.class);
                            put(1860, SynchronizationOnStringOrBoxedProcessor.class);
                            put(2184, CastArithmeticOperandProcessor.class);
                            put(4973, CompareStringsBoxedTypesWithEqualsProcessor.class);
                            put(2095, UnclosedResourcesProcessor.class);
                            put(3984, UnusedThrowableProcessor.class);
                            put(2225, ToStringReturningNullProcessor.class);
                            put(2164, MathOnFloatProcessor.class);
                            put(2167, CompareToReturnValueProcessor.class);
                            put(3032, GetClassLoaderProcessor.class);
                            put(1656, SelfAssignementProcessor.class);
                            put(3067, SynchronizationOnGetClassProcessor.class);
                            put(2204, EqualsOnAtomicClassProcessor.class);
                            put(1948, SerializableFieldInSerializableClassProcessor.class);
                            put(1854, DeadStoreProcessor.class);
                            put(2142, InterruptedExceptionProcessor.class);
                            put(2111, BigDecimalDoubleConstructorProcessor.class);
                        }
                    };

    public static final String RULE_DESCRIPTIONS =
            "\n2116: \"hashCode\" and \"toString\" should not be called on array instances\n2111: \"BigDecimal(double)\" should not be used\n2184: Math operands should be cast before assignment\n4973: Strings and Boxed types should be compared using \"equals()\"\n2167: \"compareTo\" should not return \"Integer.MIN_VALUE\"\n1854: Unused assignments should be removed\n2204: \".equals()\" should not be used to test the values of \"Atomic\" classes\n3032: JEE applications should not \"getClassLoader\"\n2142: \"InterruptedException\" should not be ignored\n2272: \"Iterator.next()\" methods should throw \"NoSuchElementException\"\n2164: Math should not be performed on floats\n1444: \"public static\" fields should be constant\n\t(incomplete: does not fix variable naming)\n1656: Variables should not be self-assigned\n1948: Fields in a \"Serializable\" class should either be transient or serializable\n3067: \"getClass\" should not be used for synchronization\n1860: Synchronization should not be based on Strings or boxed primitives\n1217: \"Thread.run()\" should not be called directly\n2225: \"toString()\" and \"clone()\" methods should not return null\n\t(incomplete: does not fix null returning clone())\n2095: Resources should be closed\n3984: Exception should not be created without being thrown\n2755: XML parsers should not be vulnerable to XXE attacks\n\t(incomplete: This processor is a WIP and currently supports a subset of rule 2755. See Sorald\'s documentation for details.)";

    public static Class<? extends SoraldAbstractProcessor<?>> getProcessor(int key) {
        return RULE_KEY_TO_PROCESSOR.get(key);
    }
}
