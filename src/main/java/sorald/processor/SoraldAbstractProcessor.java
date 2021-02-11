package sorald.processor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import sorald.annotations.ProcessorAnnotation;
import sorald.event.EventHelper;
import sorald.event.SoraldEventHandler;
import sorald.event.models.CrashEvent;
import sorald.event.models.RepairEvent;
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
        Arrays.stream(getClass().getDeclaredMethods())
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
     * <p>This method is only to be called on elements that appear very close to a violation.
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
     * @return true if the repair proceeded without crashing, false if errors were encountered.
     */
    public final boolean repair(E element) {
        try {
            repairInternal(element);
            return true;
        } catch (Exception e) {
            fireCrashEvent("repairInternal", e);
            return false;
        }
    }

    /**
     * Same as the general description of {@link SoraldAbstractProcessor#canRepair(CtElement)}.
     *
     * <p>Note that this method is only ever called on an element that is deemed to be very close to
     * a violation that concerns the implementing processor (most often intersecting with the
     * violation location, otherwise at least appearing on the same line). If the processor
     * implementing this method operates on elements that never appear close to other elements of
     * its kind, then you can be confident that any element passed to this method does violate the
     * considered rule.
     *
     * <p>For processors operating on highly granular elements, such as expressions, there is
     * typically a need to perform some sanity checks, as other similar elements may appear very
     * closely or even be nested.
     *
     * <p>Also Note that a processor gets ONE chance to repair a violation. If this method returns
     * true, the violating element is passed to the {@link
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
        CtElement elementClone = element.clone();
        elementClone.setParent(element.getParent());
        try {
            assert !processedViolations.contains(bestFits.get(element));

            repair(element);

            EventHelper.fireEvent(
                    new RepairEvent(bestFits.get(element), elementClone, false), eventHandlers);

            processedViolations.add(bestFits.get(element));
        } catch (Exception e) {
            fireCrashEvent("process", e);

            if (bestFits != null && bestFits.containsKey(element)) {
                EventHelper.fireEvent(
                        new RepairEvent(bestFits.get(element), elementClone, true), eventHandlers);
            }
        }
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

    private void fireCrashEvent(String methodName, Exception e) {
        EventHelper.fireEvent(
                new CrashEvent("Crash in " + getClass().getCanonicalName() + "::" + methodName, e),
                eventHandlers);
    }
}
