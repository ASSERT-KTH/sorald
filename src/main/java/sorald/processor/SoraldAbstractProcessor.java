package sorald.processor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
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
    private int maxFixes = Integer.MAX_VALUE;
    private int nbFixes = 0;
    private List<SoraldEventHandler> eventHandlers;

    public SoraldAbstractProcessor() {}

    public SoraldAbstractProcessor<E> setRuleViolations(Set<RuleViolation> ruleViolations) {
        this.ruleViolations = new HashSet<>(ruleViolations);
        return this;
    }

    public SoraldAbstractProcessor setMaxFixes(int maxFixes) {
        this.maxFixes = maxFixes;
        return this;
    }

    public SoraldAbstractProcessor setNbFixes(int nbFixes) {
        this.nbFixes = nbFixes;
        return this;
    }

    public SoraldAbstractProcessor<?> setEventHandlers(List<SoraldEventHandler> eventHandlers) {
        this.eventHandlers = eventHandlers;
        return this;
    }

    public int getNbFixes() {
        return this.nbFixes;
    }

    public boolean isToBeProcessedAccordingToStandards(E element) {
        return (this.nbFixes < this.maxFixes) && this.isToBeProcessedAccordingToSonar(element);
    }

    public boolean isToBeProcessedAccordingToSonar(E element) {
        if (element == null) {
            return false;
        }
        if (!element.getPosition().isValidPosition()) {
            return false;
        }
        int line = element.getPosition().getLine();
        String file = element.getPosition().getFile().getAbsolutePath();

        try (Stream<String> lines = Files.lines(Paths.get(file))) {
            if (lines.skip(line - 1).findFirst().get().contains("NOSONAR")) {
                return false;
            }
        } catch (IOException e) {
        }

        for (RuleViolation ruleViolation : ruleViolations) {
            if (FileUtils.pathAbsNormEqual(ruleViolation.getFileName(), file)
                    && elementIntersectsViolation(element, ruleViolation)) {
                return true;
            }
        }
        return false;
    }

    private static boolean elementIntersectsViolation(CtElement element, RuleViolation violation) {
        int[] lineSeps = element.getPosition().getCompilationUnit().getLineSeparatorPositions();

        int vStartLine = violation.getStartLine();
        int vEndLine = violation.getEndLine();
        int violationSourceStart =
                (vStartLine == 1 ? 0 : lineSeps[vStartLine - 2]) + violation.getStartCol();
        int violationSourceEnd =
                (vEndLine == 1 ? 0 : lineSeps[vEndLine - 2]) + violation.getEndCol();

        int elemSourceStart = element.getPosition().getSourceStart();
        int elemSourceEnd = element.getPosition().getSourceEnd();

        return pointsIntersect(
                violationSourceStart, violationSourceEnd, elemSourceStart, elemSourceEnd);
    }

    private static boolean pointsIntersect(int startLhs, int endLhs, int startRhs, int endRhs) {
        return startRhs <= endLhs && endRhs >= startLhs;
    }

    @Override
    public void process(E element) {
        final String ruleKey = getRuleKey();
        final String elementPosition = element.getPosition().toString();

        EventHelper.fireEvent(new RepairEvent(ruleKey, elementPosition), eventHandlers);
        UniqueTypesCollector.getInstance().collect(element);
        this.nbFixes++;
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
