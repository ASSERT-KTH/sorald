package sorald.processor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import sorald.FileUtils;
import sorald.UniqueTypesCollector;
import sorald.annotations.ProcessorAnnotation;
import sorald.event.EventHelper;
import sorald.event.EventType;
import sorald.event.SoraldEvent;
import sorald.event.SoraldEventHandler;
import sorald.sonar.RuleViolation;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtElement;

/** superclass for all processors */
public abstract class SoraldAbstractProcessor<E extends CtElement> extends AbstractProcessor<E> {
    private Set<RuleViolation> ruleViolations;
    private Set<RuleViolation> unprocessedViolations;
    private int maxFixes = Integer.MAX_VALUE;
    private List<SoraldEventHandler> eventHandlers;

    @SuppressWarnings("unchecked")
    public SoraldAbstractProcessor() {
        super();
        // we must override the processed element types as they depend on the concrete type of
        // the process method, which with type erasure will always be CtElement for
        // SoraldAbstractProcessor::process
        clearProcessedElementType();
        Arrays.stream(getClass().getMethods())
                .filter(meth -> meth.getName().equals("repair") && meth.getParameterCount() == 1)
                .map(Method::getParameterTypes)
                .flatMap(Arrays::stream)
                .filter(CtElement.class::isAssignableFrom)
                .map(paramType -> (Class<CtElement>) paramType)
                .forEach(this::addProcessedElementType);
    }

    /**
     * Pass a candidate element that appears in the vicinity of a violation for the processor to
     * inspect. This may or may not always be the exact element that should be repaired. For
     * example, when repairing something involving a method call, there may be nested calls that are
     * not actually the violating parties, but still appear in the correct vicinity.
     *
     * <p>Note that a processor gets ONE chance to repair a violation. If it returns true, the
     * violating element is passed to the {@link SoraldAbstractProcessor#repair(CtElement)} method,
     * and the violation is consumed.
     *
     * @param candidate A candidate element to inspect.
     * @return true if the processor can repair the violation based on this element.
     */
    public abstract boolean canRepair(E candidate);

    /**
     * Repair a violating element. An element is only passed to this method after having been
     * accepted by {@link SoraldAbstractProcessor#canRepair(CtElement)}.
     *
     * @param element An element to repair.
     */
    public abstract void repair(E element);

    public SoraldAbstractProcessor<E> setRuleViolations(Set<RuleViolation> ruleViolations) {
        this.ruleViolations = Collections.unmodifiableSet(ruleViolations);
        unprocessedViolations = new HashSet<>(ruleViolations);
        return this;
    }

    public SoraldAbstractProcessor setMaxFixes(int maxFixes) {
        this.maxFixes = maxFixes;
        return this;
    }

    public SoraldAbstractProcessor<?> setEventHandlers(List<SoraldEventHandler> eventHandlers) {
        this.eventHandlers = eventHandlers;
        return this;
    }

    public int getNbFixes() {
        return ruleViolations.size() - unprocessedViolations.size();
    }

    public boolean isToBeProcessedAccordingToStandards(E element, RuleViolation violation) {
        return (getNbFixes() < this.maxFixes)
                && this.isToBeProcessedAccordingToSonar(element, violation);
    }

    public boolean isToBeProcessedAccordingToSonar(E element, RuleViolation violation) {
        if (element == null) {
            return false;
        }
        if (!element.getPosition().isValidPosition()) {
            return false;
        }
        int line = element.getPosition().getLine();
        String file = element.getPosition().getFile().getAbsolutePath();

        return violation.getLineNumber() == line
                && FileUtils.pathAbsNormEqual(violation.getFileName(), file);
    }

    @Override
    public final void process(E element) {
        final String ruleKey = getRuleKey();
        final String elementPosition = element.getPosition().toString();

        repair(element);

        EventHelper.fireEvent(new RepairEvent(ruleKey, elementPosition), eventHandlers);
        UniqueTypesCollector.getInstance().collect(element);
    }

    @Override
    public final boolean isToBeProcessed(E element) {
        if (!canRepair(element)) {
            return false;
        }

        Optional<RuleViolation> applicableViolation =
                unprocessedViolations.stream()
                        .filter(
                                violation ->
                                        isToBeProcessedAccordingToStandards(element, violation))
                        .findFirst();

        if (applicableViolation.isPresent()) {
            unprocessedViolations.remove(applicableViolation.get());
            return true;
        } else {
            return false;
        }
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
}
