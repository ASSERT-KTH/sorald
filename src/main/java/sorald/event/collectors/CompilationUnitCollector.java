package sorald.event.collectors;

import static sorald.support.IdentityHashSet.newIdentityHashSet;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import sorald.event.EventType;
import sorald.event.SoraldEvent;
import sorald.event.SoraldEventHandler;
import sorald.event.models.RepairEvent;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;

/** Event handler that collects compilation units that receive repairs. */
public class CompilationUnitCollector implements SoraldEventHandler {
    private final Map<Path, CtCompilationUnit> pathToCu = new HashMap<>();

    /**
     * If event is a {@link RepairEvent}, collect the compilation unit from its violating element.
     *
     * @param event An event.
     */
    @Override
    public void registerEvent(SoraldEvent event) {
        if (event.type() == EventType.REPAIR) {
            collectCompilationUnit(((RepairEvent) event).getElement());
        }
    }
    /** @return All unique compilation units that have been collected from repair events. */
    public Set<CtCompilationUnit> getCollectedCompilationUnits() {
        return newIdentityHashSet(pathToCu.values());
    }

    /**
     * Collect the compilation unit that this element belongs to.
     *
     * @param element An element from which to collect the compilation unit.
     */
    void collectCompilationUnit(CtElement element) {
        Path filePath = element.getPosition().getFile().toPath().toAbsolutePath();
        CtType<?> type =
                (element instanceof CtType) ? (CtType<?>) element : element.getParent(CtType.class);
        CtCompilationUnit cu = getCompilationUnit(type);
        pathToCu.put(filePath, cu);
    }

    private static CtCompilationUnit getCompilationUnit(CtType<?> type) {
        return type.getFactory().CompilationUnit().getOrCreate(type);
    }
}
