package sorald;

import java.util.Collection;
import java.util.IdentityHashMap;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.ForceImportProcessor;
import spoon.reflect.visitor.LexicalScope;

public class SelectiveForceImport extends ForceImportProcessor {
    // used as a set, don't care about the value
    // use identity rather than equality to identify existing references to avoid mistaking clones
    // for originals
    private final IdentityHashMap<CtTypeReference<?>, Boolean> excludedReferences;

    public SelectiveForceImport(Collection<CtTypeReference<?>> references) {
        excludedReferences = new IdentityHashMap<>();
        references.forEach(ref -> excludedReferences.put(ref, true));
    }

    @Override
    protected void handleTypeReference(
            CtTypeReference<?> reference, LexicalScope nameScope, CtRole role) {
        if (!excludedReferences.containsKey(reference)) {
            super.handleTypeReference(reference, nameScope, role);
        }
    }
}
