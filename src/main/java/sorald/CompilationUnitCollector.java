package sorald;

import static sorald.support.IdentityHashSet.newIdentityHashSet;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;

/* Only add the CtType object if it does not exist in the map yet */
public class CompilationUnitCollector {
    private static CompilationUnitCollector uniqueTypesCollector;

    private final Map<Path, CtCompilationUnit> pathToCu;
    private final SoraldConfig config;

    private CompilationUnitCollector(SoraldConfig config) {
        this.config = config;
        pathToCu = new HashMap<>();
    }

    public static CompilationUnitCollector getInstance() {
        if (uniqueTypesCollector == null) {
            throw new IllegalStateException("must call reset first");
        }
        return uniqueTypesCollector;
    }

    public static void reset(SoraldConfig config) {
        CompilationUnitCollector.uniqueTypesCollector = new CompilationUnitCollector(config);
    }

    public Set<CtCompilationUnit> getCollectedCompilationUnits() {
        return newIdentityHashSet(pathToCu.values());
    }

    public void collect(CtElement element) {
        CtType<?> topLevelType =
                (element instanceof CtType) && ((CtType<?>) element).isTopLevel()
                        ? (CtType<?>) element
                        : element.getParent(CtType.class)
                                .getReference()
                                .getTopLevelType()
                                .getTypeDeclaration();
        Path filePath = element.getPosition().getFile().toPath().toAbsolutePath();

        if (config.getFileOutputStrategy() == FileOutputStrategy.CHANGED_ONLY) {
            removeOriginalSourceVersion(filePath);
        }

        CtCompilationUnit cu =
                topLevelType.getFactory().CompilationUnit().getOrCreate(topLevelType);
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
    private void removeOriginalSourceVersion(Path filePath) {
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
