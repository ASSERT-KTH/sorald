package sorald;

import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtModule;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.support.DefaultOutputDestinationHandler;
import spoon.support.OutputDestinationHandler;

/** Class with utility methods for determining output paths. */
class OutputPaths {

    /**
     * Compute the output path for the given compilation unit with the provided root directory as
     * the project root.
     *
     * @param cu A compilation unit.
     * @param rootDir The root directory to print output in.
     * @return Output path for the compilation unit.
     */
    static Path resolveOutputPath(CtCompilationUnit cu, File rootDir) {
        OutputDestinationHandler destHandler =
                new DefaultOutputDestinationHandler(rootDir, cu.getFactory().getEnvironment());
        CtModule mod = cu.getDeclaredModule();
        CtPackage pack = cu.getDeclaredPackage();
        CtType<?> type = findPrimaryType(cu).orElseGet(() -> guessIntendedPrimaryType(cu));
        return destHandler.getOutputPath(mod, pack, type).normalize();
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
     * In the event that a compilation unit has no top-level type that matches the file name, this
     * method can be used to find the most likely "intended" top-level type.
     *
     * @param cu A compilation unit that lacks a well-defined primary type.
     * @return The most likely primary type of this compilation unit.
     */
    private static CtType<?> guessIntendedPrimaryType(CtCompilationUnit cu) {
        Function<CtType<?>, Integer> visibilityOrdering =
                (type) -> {
                    if (type.isPublic()) {
                        return 1;
                    } else if (!(type.isPrivate() || type.isProtected())) { // is package-private
                        return 2;
                    } else {
                        // is private or protected, which is not legal for a top-level type
                        return 3;
                    }
                };

        return cu.getDeclaredTypes().stream()
                .filter(CtType::isTopLevel)
                .min(Comparator.comparing(visibilityOrdering))
                .orElseThrow();
    }
}
