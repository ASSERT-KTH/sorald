package sorald.sonar;

import org.sonar.check.Rule;
import org.sonar.java.checks.*;
import org.sonar.java.checks.naming.MethodNamedEqualsCheck;
import org.sonar.java.checks.naming.MethodNamedHashcodeOrEqualCheck;
import org.sonar.java.checks.serialization.CustomSerializationMethodCheck;
import org.sonar.java.checks.serialization.ExternalizableClassConstructorCheck;
import org.sonar.java.checks.serialization.SerializableFieldInSerializableClassCheck;
import org.sonar.java.checks.serialization.SerializableObjectInSessionCheck;
import org.sonar.java.checks.serialization.SerializableSuperConstructorCheck;
import org.sonar.java.checks.spring.ControllerWithSessionAttributesCheck;
import org.sonar.java.checks.spring.SpringComponentWithWrongScopeCheck;
import org.sonar.java.checks.spring.SpringIncompatibleTransactionalCheck;
import org.sonar.java.checks.spring.SpringScanDefaultPackageCheck;
import org.sonar.java.checks.synchronization.DoubleCheckedLockingCheck;
import org.sonar.java.checks.synchronization.SynchronizationOnGetClassCheck;
import org.sonar.java.checks.synchronization.TwoLocksWaitCheck;
import org.sonar.java.checks.synchronization.ValueBasedObjectUsedForLockCheck;
import org.sonar.java.checks.unused.UnusedReturnedDataCheck;
import org.sonar.java.checks.unused.UnusedThrowableCheck;
import org.sonar.java.se.checks.*;
import org.sonar.plugins.java.api.JavaFileScanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** Class for easily accessing Sonar check classes. */
@SuppressWarnings({"unchecked", "UnstableApiUsage"})
public class Checks {

    private static final Map<CheckType, Map<String, Class<? extends JavaFileScanner>>>
            CHECKS_BY_TYPE;

    public enum CheckType {
        BUG,
        VULNERABILITY,
        CODE_SMELL,
        SECURITY_HOTSPOT;
    }

    /**
     * @param checkType A check type.
     * @return All checks of the given type.
     */
    public static List<Class<? extends JavaFileScanner>> getChecksByType(CheckType checkType) {
        return new ArrayList<>(CHECKS_BY_TYPE.get(checkType).values());
    }

    /**
     * Get a specific check by key.
     *
     * @param key The key of the check.
     * @return The check class corresponding to the key.
     */
    public static Class<? extends JavaFileScanner> getCheck(String key) {
        final String strippedKey = stripDigits(key);
        return CHECKS_BY_TYPE.values().stream()
                .map(checks -> checks.get(strippedKey))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no rule with key " + strippedKey));
    }

    /** @return All Sonar-Java checks that Sorald currently keeps track of. */
    public static List<Class<? extends JavaFileScanner>> getAllChecks() {
        return CHECKS_BY_TYPE.values().stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * @param checkClass A Sonar-Java check class.
     * @return the numeric identifier of the rule related to the given check class. Non-digits are
     *     stripped, so e.g. S1234 becomes 1234.
     */
    public static String getRuleKey(Class<? extends JavaFileScanner> checkClass) {
        return Arrays.stream(checkClass.getAnnotationsByType(Rule.class))
                .map(Rule::key)
                .map(Checks::stripDigits)
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        checkClass.getName() + " does not have a key"));
    }

    private static String stripDigits(String s) {
        return s.replaceAll("[^\\d]+", "");
    }

    private static Map<String, Class<? extends JavaFileScanner>> createKeyToCheckMap(
            Class<? extends JavaFileScanner>... checksToAdd) {
        Map<String, Class<? extends JavaFileScanner>> keyToCheck = new HashMap<>();
        for (Class<? extends JavaFileScanner> check : checksToAdd) {
            keyToCheck.put(getRuleKey(check), check);
        }
        return Collections.unmodifiableMap(keyToCheck);
    }

    static {
        Map<CheckType, Map<String, Class<? extends JavaFileScanner>>> typeToChecks = new EnumMap<>(CheckType.class);

        typeToChecks.put(
                CheckType.BUG,
                createKeyToCheckMap(
                        ControllerWithSessionAttributesCheck.class,
                        SpringScanDefaultPackageCheck.class,
                        TwoLocksWaitCheck.class,
                        PreparedStatementAndResultSetCheck.class,
                        ThreadSleepCheck.class,
                        PrintfFailCheck.class,
                        ThreadWaitCallCheck.class,
                        SpringIncompatibleTransactionalCheck.class,
                        DoubleCheckedLockingCheck.class,
                        GettersSettersOnRightFieldCheck.class,
                        RunFinalizersCheck.class,
                        ScheduledThreadPoolExecutorZeroCheck.class,
                        ReuseRandomCheck.class,
                        ObjectFinalizeOverloadedCheck.class,
                        ReturnInFinallyCheck.class,
                        ThreadLocalCleanupCheck.class,
                        CompareStringsBoxedTypesWithEqualsCheck.class,
                        InputStreamReadCheck.class,
                        CompareToNotOverloadedCheck.class,
                        IterableIteratorCheck.class,
                        OverwrittenKeyCheck.class,
                        DateFormatWeekYearCheck.class,
                        UnusedThrowableCheck.class,
                        CollectionSizeAndArrayLengthCheck.class,
                        AllBranchesAreIdenticalCheck.class,
                        SynchronizedOverrideCheck.class,
                        ValueBasedObjectUsedForLockCheck.class,
                        AssertOnBooleanVariableCheck.class,
                        VolatileVariablesOperationsCheck.class,
                        SynchronizationOnGetClassCheck.class,
                        DoubleCheckedLockingAssignmentCheck.class,
                        StringCallsBeyondBoundsCheck.class,
                        RawByteBitwiseOperationsCheck.class,
                        SyncGetterAndSetterCheck.class,
                        StaticMultithreadedUnsafeFieldsCheck.class,
                        NullShouldNotBeUsedWithOptionalCheck.class,
                        DoublePrefixOperatorCheck.class,
                        WrongAssignmentOperatorCheck.class,
                        UnusedReturnedDataCheck.class,
                        InappropriateRegexpCheck.class,
                        NotifyCheck.class,
                        SynchronizedFieldAssignmentCheck.class,
                        SerializableObjectInSessionCheck.class,
                        WaitInSynchronizeCheck.class,
                        ForLoopFalseConditionCheck.class,
                        ForLoopIncrementSignCheck.class,
                        TransactionalMethodVisibilityCheck.class,
                        ServletInstanceFieldCheck.class,
                        ToStringReturningNullCheck.class,
                        EqualsOnAtomicClassCheck.class,
                        IgnoredReturnValueCheck.class,
                        ConfusingOverloadCheck.class,
                        CollectionInappropriateCallsCheck.class,
                        SillyEqualsCheck.class,
                        PrimitiveWrappersInTernaryOperatorCheck.class,
                        InterruptedExceptionCheck.class,
                        ThreadOverridesRunCheck.class,
                        LongBitsToDoubleOnIntCheck.class,
                        UselessIncrementCheck.class,
                        SillyStringOperationsCheck.class,
                        NonSerializableWriteCheck.class,
                        ArrayHashCodeAndToStringCheck.class,
                        CollectionCallingItselfCheck.class,
                        BigDecimalDoubleConstructorCheck.class,
                        InvalidDateValuesCheck.class,
                        ReflectionOnNonRuntimeAnnotationCheck.class,
                        CustomSerializationMethodCheck.class,
                        ExternalizableClassConstructorCheck.class,
                        ClassComparedByNameCheck.class,
                        DuplicateConditionIfElseIfCheck.class,
                        SynchronizationOnStringOrBoxedCheck.class,
                        HasNextCallingNextCheck.class,
                        IdenticalOperandOnBinaryExpressionCheck.class,
                        LoopExecutingAtMostOnceCheck.class,
                        SelfAssignementCheck.class,
                        StringBufferAndBuilderWithCharCheck.class,
                        MethodNamedHashcodeOrEqualCheck.class,
                        ThreadRunCheck.class,
                        MethodNamedEqualsCheck.class,
                        DoubleBraceInitializationCheck.class,
                        VolatileNonPrimitiveFieldCheck.class,
                        ToArrayCheck.class,
                        AbsOnNegativeCheck.class,
                        IgnoredStreamReturnValueCheck.class,
                        IteratorNextExceptionCheck.class,
                        CompareToResultTestCheck.class,
                        CastArithmeticOperandCheck.class,
                        ShiftOnIntOrLongCheck.class,
                        CompareToReturnValueCheck.class,
                        ImmediateReverseBoxingCheck.class,
                        EqualsArgumentTypeCheck.class,
                        InnerClassOfNonSerializableCheck.class,
                        SerializableSuperConstructorCheck.class,
                        ParameterReassignedToCheck.class,
                        EqualsOverridenWithHashCodeCheck.class,
                        ObjectFinalizeOverridenCallsSuperFinalizeCheck.class,
                        SpringComponentWithWrongScopeCheck.class,
                        ConstructorInjectionCheck.class,
                        ClassWithoutHashCodeInHashStructureCheck.class,
                        InstanceOfAlwaysTrueCheck.class,
                        NullDereferenceInConditionalCheck.class,
                        FloatEqualityCheck.class,
                        IfConditionAlwaysTrueOrFalseCheck.class,
                        ObjectFinalizeCheck.class,
                        GetClassLoaderCheck.class,
                        MathOnFloatCheck.class,
                        SymmetricEqualsCheck.class,
                        ObjectOutputStreamCheck.class,
                        NoWayOutLoopCheck.class,
                        UnclosedResourcesCheck.class,
                        DivisionByZeroCheck.class,
                        LocksNotUnlockedCheck.class,
                        StreamConsumedCheck.class,
                        StreamNotConsumedCheck.class,
                        OptionalGetBeforeIsPresentCheck.class,
                        MinMaxRangeCheck.class,
                        ConditionalUnreachableCodeCheck.class,
                        NullDereferenceCheck.class,
                        NonNullSetToNullCheck.class,
                        CustomUnclosedResourcesCheck.class));

        typeToChecks.put(
                CheckType.VULNERABILITY,
                createKeyToCheckMap(
                        // , add vulnerability checks here
                        ));

        typeToChecks.put(
                CheckType.SECURITY_HOTSPOT,
                createKeyToCheckMap(
                        // , add sec hotspot checks here
                        ));

        typeToChecks.put(
                CheckType.CODE_SMELL,
                createKeyToCheckMap(
                        DeadStoreCheck.class, SerializableFieldInSerializableClassCheck.class));

        CHECKS_BY_TYPE = Collections.unmodifiableMap(typeToChecks);

        // sanity check: all CheckType values should be accounted for
        for (CheckType type : CheckType.values()) {
            assert CHECKS_BY_TYPE.containsKey(type);
        }

    }
}
