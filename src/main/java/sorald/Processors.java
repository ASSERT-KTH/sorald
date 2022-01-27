package sorald;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import sorald.processor.*;

/**
 * This class is partially generated. It is fine to edit non-generated code as per usual, but don't
 * change any of the generated fields unless you know precisely what you are doing.
 */
public class Processors {
    private Processors() {}

    // GENERATED FIELD
    private static final Map<String, Class<? extends SoraldAbstractProcessor<?>>>
            RULE_KEY_TO_PROCESSOR =
                    new java.util.HashMap<>() {
                        {
                            put("S1068", UnusedPrivateFieldProcessor.class);
                            put("S1118", UtilityClassWithPublicConstructorProcessor.class);
                            put("S1132", StringLiteralInsideEqualsProcessor.class);
                            put("S1155", CollectionIsEmptyProcessor.class);
                            put("S1217", ThreadRunProcessor.class);
                            put("S1444", PublicStaticFieldShouldBeFinalProcessor.class);
                            put("S1481", UnusedLocalVariableProcessor.class);
                            put("S1596", CollectionsEmptyConstantsProcessor.class);
                            put("S1656", SelfAssignementProcessor.class);
                            put("S1854", DeadStoreProcessor.class);
                            put("S1860", SynchronizationOnStringOrBoxedProcessor.class);
                            put("S1948", SerializableFieldInSerializableClassProcessor.class);
                            put("S2057", SerialVersionUidProcessor.class);
                            put("S2095", UnclosedResourcesProcessor.class);
                            put("S2097", EqualsArgumentTypeProcessor.class);
                            put("S2111", BigDecimalDoubleConstructorProcessor.class);
                            put("S2116", ArrayHashCodeAndToStringProcessor.class);
                            put("S2142", InterruptedExceptionProcessor.class);
                            put("S2164", MathOnFloatProcessor.class);
                            put("S2167", CompareToReturnValueProcessor.class);
                            put("S2184", CastArithmeticOperandProcessor.class);
                            put("S2204", EqualsOnAtomicClassProcessor.class);
                            put("S2225", ToStringReturningNullProcessor.class);
                            put("S2272", IteratorNextExceptionProcessor.class);
                            put("S2755", XxeProcessingProcessor.class);
                            put("S3032", GetClassLoaderProcessor.class);
                            put("S3067", SynchronizationOnGetClassProcessor.class);
                            put("S3984", UnusedThrowableProcessor.class);
                            put("S4065", ThreadLocalWithInitial.class);
                            put("S4973", CompareStringsBoxedTypesWithEqualsProcessor.class);
                        }
                    };

    // GENERATED FIELD
    public static final String RULE_DESCRIPTIONS =
            "S1068: Unused \"private\" fields should be removed\nS1118: Utility classes should not have public constructors\n\t(incomplete: Only handles implicit public constructor)\nS1132: Strings literals should be placed on the left side when checking for equality\nS1155: Collection.isEmpty() should be used to test for emptiness\nS1217: \"Thread.run()\" should not be called directly\nS1444: \"public static\" fields should be constant\n\t(incomplete: does not fix variable naming)\nS1481: Unused local variables should be removed\nS1596: \"Collections.EMPTY_LIST\", \"EMPTY_MAP\", and \"EMPTY_SET\" should not be used\nS1656: Variables should not be self-assigned\nS1854: Unused assignments should be removed\nS1860: Synchronization should not be based on Strings or boxed primitives\nS1948: Fields in a \"Serializable\" class should either be transient or serializable\nS2057: Every class implementing Serializable should declare a static final serialVersionUID.\n\t(incomplete: This processor does not address the case where the class already has a serialVersionUID with a non long type.)\nS2095: Resources should be closed\nS2097: \"equals(Object obj)\" should test argument type\nS2111: \"BigDecimal(double)\" should not be used\nS2116: \"hashCode\" and \"toString\" should not be called on array instances\nS2142: \"InterruptedException\" should not be ignored\nS2164: Math should not be performed on floats\nS2167: \"compareTo\" should not return \"Integer.MIN_VALUE\"\nS2184: Math operands should be cast before assignment\nS2204: \".equals()\" should not be used to test the values of \"Atomic\" classes\nS2225: \"toString()\" and \"clone()\" methods should not return null\n\t(incomplete: does not fix null returning clone())\nS2272: \"Iterator.next()\" methods should throw \"NoSuchElementException\"\nS2755: XML parsers should not be vulnerable to XXE attacks\n\t(incomplete: This processor is a WIP and currently supports a subset of rule 2755. See Sorald\'s documentation for details.)\nS3032: JEE applications should not \"getClassLoader\"\nS3067: \"getClass\" should not be used for synchronization\nS3984: Exception should not be created without being thrown\nS4065: \"ThreadLocal.withInitial\" should be preferred\nS4973: Strings and Boxed types should be compared using \"equals()\"";

    public static Class<? extends SoraldAbstractProcessor<?>> getProcessor(String key) {
        return RULE_KEY_TO_PROCESSOR.get(key);
    }

    /** @return A list of all processors sorted by name. */
    public static List<Class<? extends SoraldAbstractProcessor<?>>> getAllProcessors() {
        return RULE_KEY_TO_PROCESSOR.values().stream()
                .sorted(Comparator.comparing(Class::getName))
                .collect(Collectors.toList());
    }
}
