package sorald.processor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import sorald.UniqueTypesCollector;
import sorald.annotations.ProcessorAnnotation;
import sorald.event.EventHelper;
import sorald.event.EventType;
import sorald.event.SoraldEvent;
import sorald.event.SoraldEventHandler;
import sorald.event.models.CrashEvent;
import sorald.sonar.RuleViolation;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtElement;

/** superclass for all processors */
public abstract class SoraldAbstractProcessor<E extends CtElement> extends AbstractProcessor<E> {
    private int maxFixes = Integer.MAX_VALUE;
    private List<SoraldEventHandler> eventHandlers;
    private final List<RuleViolation> processedViolations;

    private Map<CtElement, RuleViolation> bestFits;

    @SuppressWarnings("unchecked")
    public SoraldAbstractProcessor() {
        super();
        // we must override the processed element types as they depend on the concrete type of
        // the process method, which with type erasure will always be CtElement for
        // SoraldAbstractProcessor::process
        clearProcessedElementType();
        Arrays.stream(getClass().getMethods())
                .filter(
                        meth ->
                                meth.getName().equals("repairInternal")
                                        && meth.getParameterCount() == 1)
                .map(Method::getParameterTypes)
                .flatMap(Arrays::stream)
                .filter(CtElement.class::isAssignableFrom)
                .filter(cls -> !cls.equals(CtElement.class))
                .map(paramType -> (Class<CtElement>) paramType)
                .forEach(this::addProcessedElementType);

        // This might become false if we ever add a processor for CtElement. Which we probably
        // should not, it seems to always make sense to target a more specific type.
        assert !getProcessedElementTypes().isEmpty();

        processedViolations = new ArrayList<>();
    }

    /**
     * Pass a candidate element that appears in the vicinity of a violation for the processor to
     * inspect. This may or may not always be the exact element that should be repaired. For
     * example, when repairing something involving a method call, there may be nested calls that are
     * not actually the violating parties, but still appear in the correct vicinity.
     *
     * <p><b>This method does not mutate the state of the processor</b>.
     *
     * <p>This method never crashes, instead returning false if there is a problem in the concrete
     * processor.
     *
     * @param candidate A candidate element to inspect.
     * @return true if the processor can repair the violation based on this element.
     */
    public final boolean canRepair(E candidate) {
        try {
            return canRepairInternal(candidate);
        } catch (Exception e) {
            fireCrashEvent("canRepairInternal", e);
            return false;
        }
    }

    /**
     * Repair a violating element after having been accepted by {@link
     * SoraldAbstractProcessor#canRepair(CtElement)}.
     *
     * <p>This method never crashes.
     *
     * @param element An element to repair.
     */
    public final void repair(E element) {
        try {
            repairInternal(element);
        } catch (Exception e) {
            fireCrashEvent("repairInternal", e);
        }
    }

    /**
     * Same as the general description of {@link SoraldAbstractProcessor#canRepair(CtElement)}.
     *
     * <p>Note that a processor gets ONE chance to repair a violation. If this method returns true,
     * the violating element is passed to the {@link
     * SoraldAbstractProcessor#repairInternal(CtElement)} method, and the violation is consumed.
     *
     * <p>It is very important that this method <b>does not mutate the state of the processor.</b>
     * Doing so may have unexpected side effects.
     *
     * @param candidate A candidate element.
     * @return true if the processor can repair the violation based on this element.
     */
    protected abstract boolean canRepairInternal(E candidate);

    /**
     * Repair a violating element. An element is only passed to this method after having been
     * accepted by {@link SoraldAbstractProcessor#canRepairInternal(CtElement)}.
     *
     * @param element An element to repair.
     */
    protected abstract void repairInternal(E element);

    public SoraldAbstractProcessor<E> setBestFits(Map<CtElement, RuleViolation> bestFits) {
        this.bestFits = Collections.unmodifiableMap(bestFits);
        return this;
    }

    public SoraldAbstractProcessor setMaxFixes(int maxFixes) {
        this.maxFixes = maxFixes;
        return this;
    }

    public SoraldAbstractProcessor<E> setEventHandlers(List<SoraldEventHandler> eventHandlers) {
        this.eventHandlers = eventHandlers;
        return this;
    }

    public int getNbFixes() {
        return processedViolations.size();
    }

    @Override
    public final void process(E element) {
        assert !processedViolations.contains(bestFits.get(element));

        final String ruleKey = getRuleKey();
        final String elementPosition = element.getPosition().toString();

        repair(element);

        EventHelper.fireEvent(new RepairEvent(ruleKey, elementPosition), eventHandlers);
        UniqueTypesCollector.getInstance().collect(element);

        processedViolations.add(bestFits.get(element));
    }

    @Override
    public final boolean isToBeProcessed(E element) {
        return getNbFixes() < maxFixes && bestFits.containsKey(element);
    }

    /** @return The numerical identifier of the rule this processor is related to */
    public String getRuleKey() {
        return Arrays.stream(getClass().getAnnotationsByType(ProcessorAnnotation.class))
                .map(ProcessorAnnotation::key)
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        getClass().getName() + " does not have a key"))
                .toString();
    }

    /** @return The concrete type that this processor accepts. */
    @SuppressWarnings("unchecked")
    public Class<E> getTargetType() {
        assert getProcessedElementTypes().size() == 1;
        return (Class<E>) getProcessedElementTypes().iterator().next();
    }

    /**
     * Event representing a repair. This must be public for the json.org to be able to introspect it
     * and produce the nice JSON output.
     */
    public static class RepairEvent implements SoraldEvent {
        private final String ruleKey;
        private final String ruleViolationPosition;

        public RepairEvent(String ruleKey, String ruleViolationPosition) {
            this.ruleKey = ruleKey;
            this.ruleViolationPosition = ruleViolationPosition;
        }

        @Override
        public EventType type() {
            return EventType.REPAIR;
        }

        public String getRuleKey() {
            return ruleKey;
        }

        public String getRuleViolationPosition() {
            return ruleViolationPosition;
        }
    }

    private void fireCrashEvent(String methodName, Exception e) {
        EventHelper.fireEvent(
                new CrashEvent("Crash in " + getClass().getCanonicalName() + "::" + methodName, e),
                eventHandlers);
    }
}
