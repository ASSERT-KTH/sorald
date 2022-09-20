package sorald;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.ForceImportProcessor;
import spoon.reflect.visitor.LexicalScope;

/**
 * Processor that force-imports type references only if they are not present in the excluded
 * references.
 */
public class SelectiveForceImport extends ForceImportProcessor {
    private final Set<CtTypeReference<?>> excludedReferences;

    /**
     * @param referencesToIgnore A collection of references to ignore when force-importing.
     */
    public SelectiveForceImport(Collection<CtTypeReference<?>> referencesToIgnore) {
        // use identity rather than equality to identify existing references to avoid mistaking
        // clones for originals
        excludedReferences = Collections.newSetFromMap(new IdentityHashMap<>());
        excludedReferences.addAll(referencesToIgnore);
    }

    @Override
    protected void handleTypeReference(
            CtTypeReference<?> reference, LexicalScope nameScope, CtRole role) {
        if (!excludedReferences.contains(reference)) {
            super.handleTypeReference(reference, nameScope, role);
        }
    }
}
