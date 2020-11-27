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

public class BestFitScanner<E extends CtElement> extends CtScanner {
    private final Set<RuleViolation> violations;
    private final SoraldAbstractProcessor<E> processor;

    private final Map<RuleViolation, List<E>> onSameLine;
    private final Map<RuleViolation, List<E>> intersecting;

    public static <E extends CtElement> Map<CtElement, RuleViolation> calculateBestFits(
            CtElement element,
            Set<RuleViolation> violations,
            SoraldAbstractProcessor<E> processor) {
        var scanner = new BestFitScanner<>(violations, processor);
        scanner.scan(element);

        Map<CtElement, RuleViolation> bestFitsMap = new IdentityHashMap<>();
        for (var violation : violations) {
            scanner.getBestFit(violation).ifPresent(e -> bestFitsMap.put(e, violation));
        }
        return bestFitsMap;
    }

    private BestFitScanner(Set<RuleViolation> violations, SoraldAbstractProcessor<E> processor) {
        this.violations = Collections.unmodifiableSet(violations);
        this.processor = processor;
        onSameLine = new HashMap<>();
        intersecting = new HashMap<>();
    }

    @Override
    protected void enter(CtElement e) {
        if (!processor.getTargetType().isAssignableFrom(e.getClass())
                || !processor.canRepair(processor.getTargetType().cast(e))) {
            return;
        }
        E candidate = processor.getTargetType().cast(e);

        for (RuleViolation violation : violations) {
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
    }

    private Optional<E> getBestFit(RuleViolation violation) {
        List<E> elements =
                intersecting.getOrDefault(
                        violation, onSameLine.getOrDefault(violation, Collections.emptyList()));
        return Optional.ofNullable(elements.isEmpty() ? null : elements.get(0));
    }

    private static boolean elementIntersectsViolation(CtElement element, RuleViolation violation) {
        if (!FileUtils.pathAbsNormEqual(
                violation.getFileName(), element.getPosition().getFile().getAbsolutePath())) {
            return false;
        }
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
}
