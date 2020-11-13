package sorald;

import java.util.Collection;
import java.util.IdentityHashMap;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.ForceImportProcessor;
import spoon.reflect.visitor.LexicalScope;

/**
 * Processor that force-imports type references only if they are not present in the excluded
 * references.
 */
public class SelectiveForceImport extends ForceImportProcessor {
    // use identity rather than equality to identify existing references to avoid mistaking clones
    // for originals
    private final IdentityHashMap<CtTypeReference<?>, Boolean> excludedReferences;

    /** @param referencesToIgnore A collection of references to ignore when force-importing. */
    public SelectiveForceImport(Collection<CtTypeReference<?>> referencesToIgnore) {
        excludedReferences = new IdentityHashMap<>();
        referencesToIgnore.forEach(ref -> excludedReferences.put(ref, true));
    }

    @Override
    protected void handleTypeReference(
            CtTypeReference<?> reference, LexicalScope nameScope, CtRole role) {
        if (!excludedReferences.containsKey(reference)) {
            super.handleTypeReference(reference, nameScope, role);
        }
    }
}
