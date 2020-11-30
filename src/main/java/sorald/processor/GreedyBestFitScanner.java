package sorald.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import sorald.FileUtils;
import sorald.sonar.RuleViolation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.CtScanner;

/** Scanner for greedily matching rule violations against Spoon elements. */
public class GreedyBestFitScanner<E extends CtElement> extends CtScanner {
    private final Set<RuleViolation> violations;
    private final SoraldAbstractProcessor<E> processor;

    private final Map<RuleViolation, List<E>> onSameLine;
    private final Map<RuleViolation, List<E>> intersecting;

    /**
     * Calculate a best fits mapping between Spoon elements and rule violations.
     *
     * <p>First it tries to find a Spoon element that intersects the rule violation's position. If
     * that fails, it searches all Spoon elements that start on the same line that the rule
     * violation starts on. Only elements that return true for {@link
     * SoraldAbstractProcessor#canRepair(CtElement)} are considered as potential best fits.
     *
     * @param element The root element to scan. This is typically the unnamed module.
     * @param violations The rule violations to find matching elements for. Must be violations of a
     *     single rule.
     * @param processor The processor for which to calculate best matches. Must be the processor for
     *     the single rule that the violations violate.
     * @return A mapping from Spoon element to an associated rule violation.
     */
    public static <E extends CtElement> Map<CtElement, RuleViolation> calculateBestFits(
            CtElement element,
            Set<RuleViolation> violations,
            SoraldAbstractProcessor<E> processor) {
        var scanner = new GreedyBestFitScanner<>(violations, processor);
        scanner.scan(element);

        Map<CtElement, RuleViolation> bestFitsMap = new IdentityHashMap<>();
        for (var violation : violations) {
            scanner.getBestFit(violation).ifPresent(e -> bestFitsMap.put(e, violation));
        }
        return bestFitsMap;
    }

    private GreedyBestFitScanner(
            Set<RuleViolation> violations, SoraldAbstractProcessor<E> processor) {
        this.violations = Collections.unmodifiableSet(violations);
        this.processor = processor;
        onSameLine = new HashMap<>();
        intersecting = new HashMap<>();
    }

    @Override
    protected void enter(CtElement e) {
        processor.setFactory(e.getFactory());
        if (!processor.getTargetType().isAssignableFrom(e.getClass())
                || !processor.canRepair(processor.getTargetType().cast(e))) {
            return;
        }
        E candidate = processor.getTargetType().cast(e);

        for (RuleViolation violation : violations) {
            if (!inSameFile(e, violation)) {
                continue;
            }

            if (startOnSameLine(candidate, violation)) {
                var elementsOnSameLine = onSameLine.getOrDefault(violation, new ArrayList<>());
                elementsOnSameLine.add(candidate);
                onSameLine.putIfAbsent(violation, elementsOnSameLine);
            }

            if (elementIntersectsViolation(candidate, violation)) {
                var intersectingElements = intersecting.getOrDefault(violation, new ArrayList<>());
                intersectingElements.add(candidate);
                intersecting.putIfAbsent(violation, intersectingElements);
            }
        }
        processor.setFactory(null);
    }

    private Optional<E> getBestFit(RuleViolation violation) {
        List<E> elements =
                intersecting.getOrDefault(
                        violation, onSameLine.getOrDefault(violation, Collections.emptyList()));
        return Optional.ofNullable(elements.isEmpty() ? null : elements.get(0));
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

    private static boolean startOnSameLine(CtElement element, RuleViolation violation) {
        return element.getPosition().getLine() == violation.getStartLine();
    }

    private static boolean inSameFile(CtElement element, RuleViolation violation) {
        return element.getPosition().isValidPosition()
                && FileUtils.pathAbsNormEqual(
                        violation.getFileName(), element.getPosition().getFile().getAbsolutePath());
    }
}
