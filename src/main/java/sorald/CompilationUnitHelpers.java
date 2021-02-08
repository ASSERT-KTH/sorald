package sorald;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.Set;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtModule;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.support.DefaultOutputDestinationHandler;

/** Helpers for dealing with {@link CtCompilationUnit}s. */
public class CompilationUnitHelpers {

    /**
     * Compute the output path for the given compilation unit with the provided root directory as
     * the project root.
     *
     * @param cu A compilation unit.
     * @param rootDir The root directory to print output in.
     * @return Output path for the compilation unit.
     */
    static Optional<Path> resolveOutputPath(CtCompilationUnit cu, File rootDir) {
        CtModule mod = cu.getDeclaredModule();
        CtPackage pack = cu.getDeclaredPackage();
        var destHandler =
                new DefaultOutputDestinationHandler(rootDir, cu.getFactory().getEnvironment());
        return findPrimaryType(cu).map(type -> destHandler.getOutputPath(mod, pack, type));
    }

    /**
     * Get the primary type from the given compilation unit. This only returns a non-empty value if
     * there is a type with the CU's file name in the CU.
     *
     * @param cu A compilation unit.
     * @return The primary type of the compilation unit, or empty if there is no clear primary type.
     */
    private static Optional<CtType<?>> findPrimaryType(CtCompilationUnit cu) {
        String primaryTypeName =
                cu.getPosition().getFile().getName().replace(Constants.JAVA_EXT, "");
        return cu.getDeclaredTypes().stream()
                .filter(CtType::isTopLevel)
                .filter(type -> type.getSimpleName().equals(primaryTypeName))
                .findFirst();
    }

    /**
     * @param types A collection of types.
     * @return All unique compilation units related to the provided types.
     */
    static Set<CtCompilationUnit> resolveCompilationUnits(Collection<CtType<?>> types) {
        Set<CtCompilationUnit> compilationUnits =
                Collections.newSetFromMap(new IdentityHashMap<>());
        types.stream()
                .map(CompilationUnitHelpers::getCompilationUnit)
                .forEach(compilationUnits::add);
        return compilationUnits;
    }

    /**
     * @param type A type.
     * @return The compilation unit of this type.
     */
    public static CtCompilationUnit getCompilationUnit(CtType<?> type) {
        return type.getFactory().CompilationUnit().getOrCreate(type);
    }
}
