package sorald.event.collectors;

import static sorald.support.IdentityHashSet.newIdentityHashSet;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import sorald.CompilationUnitHelpers;
import sorald.Constants;
import sorald.FileOutputStrategy;
import sorald.SoraldConfig;
import sorald.event.EventType;
import sorald.event.SoraldEvent;
import sorald.event.SoraldEventHandler;
import sorald.event.models.RepairEvent;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;

/** Event handler that collects compilation units that receive repairs. */
public class CompilationUnitCollector implements SoraldEventHandler {
    private final Map<Path, CtCompilationUnit> pathToCu;
    private final SoraldConfig config;

    /** @param config The current config. */
    public CompilationUnitCollector(SoraldConfig config) {
        this.config = config;
        pathToCu = new HashMap<>();
    }

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

        if (config.getFileOutputStrategy() == FileOutputStrategy.CHANGED_ONLY) {
            removeOriginalSourceCompilationUnit(filePath);
        }

        CtType<?> type =
                (element instanceof CtType) ? (CtType<?>) element : element.getParent(CtType.class);
        CtCompilationUnit cu = CompilationUnitHelpers.getCompilationUnit(type);
        pathToCu.put(filePath, cu);
    }

    /**
     * When multiple processors are executed, and the first one executed changes a file that is also
     * changed by another processor later, the file is registered as changed twice, but with
     * different locations (one is in original source dir, one is in intermediate dir). This leads
     * to a loss of edits when printing only changed files (FileOutputStrategy.CHANGED_ONLY).
     * Therefore, this method removes the original compilation unit if it exists, and filePath is
     * located inside the intermediate dir.
     */
    private void removeOriginalSourceCompilationUnit(Path filePath) {
        Path spoonedIntermediatePath =
                Paths.get(Constants.SORALD_WORKSPACE)
                        .resolve(Constants.SPOONED_INTERMEDIATE)
                        .toAbsolutePath();

        if (filePath.startsWith(spoonedIntermediatePath) && !pathToCu.containsKey(filePath)) {
            Path origFilesPath = Paths.get(config.getOriginalFilesPath());
            Path origPath =
                    origFilesPath.toFile().isFile()
                            ? origFilesPath
                            : origFilesPath.resolve(spoonedIntermediatePath.relativize(filePath));

            pathToCu.remove(origPath);
        }
    }
}
