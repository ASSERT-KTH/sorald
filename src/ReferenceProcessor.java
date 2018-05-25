package fr.inria.gforge.spoon.analysis;

import spoon.compiler.Environment;
import spoon.processing.AbstractProcessor;
import spoon.processing.FactoryAccessor;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.ReferenceTypeFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

/**
 * Finds circular dependencies between packages
 */
public class ReferenceProcessor extends AbstractProcessor<CtPackage> {

    private List<CtTypeReference<?>> ignoredTypes = new ArrayList<CtTypeReference<?>>();
    public List<List<CtPackageReference>> circularPathes = new ArrayList<List<CtPackageReference>>();

    @Override
    public void init() {
        ignoredTypes.add(getFactory().Type().createReference(Environment.class));
        ignoredTypes.add(getFactory().Type().createReference(Factory.class));
        ignoredTypes.add(getFactory().Type().createReference(FactoryAccessor.class));
    }

    Map<CtPackageReference, Set<CtPackageReference>> packRefs = new HashMap<CtPackageReference, Set<CtPackageReference>>();

    public void process(CtPackage element) {
        CtPackageReference pack = element.getReference();
        Set<CtPackageReference> refs = new HashSet<CtPackageReference>();
        for (CtType t : element.getTypes()) {
            List<CtTypeReference<?>> listReferences = Query.getReferences(t, new ReferenceTypeFilter<CtTypeReference<?>>(CtTypeReference.class));

            for (CtTypeReference<?> tref : listReferences) {
                if (tref.getPackage() != null && !tref.getPackage().equals(pack)) {
                    if (ignoredTypes.contains(tref))
                        continue;
                    refs.add(tref.getPackage());
                }
            }
        }
        if (refs.size() > 0) {
            packRefs.put(pack, refs);
        }
    }

    @Override
    public void processingDone() {
        for (CtPackageReference p : packRefs.keySet()) {
            Stack<CtPackageReference> path = new Stack<CtPackageReference>();
            path.push(p);
            scanDependencies(path);
        }
    }

    Set<CtPackageReference> scanned = new HashSet<CtPackageReference>();

    void scanDependencies(Stack<CtPackageReference> path) {
        CtPackageReference ref = path.peek();
        // return if already scanned
        if (scanned.contains(ref)) {
            return;
        }
        scanned.add(ref);
        Set<CtPackageReference> refs = packRefs.get(ref);
        if (refs != null) {
            for (CtPackageReference p : refs) {
                if (path.contains(p)) {
                    List<CtPackageReference> circularPath = new ArrayList<CtPackageReference>(
                            path.subList(path.indexOf(p), path.size()));
                    circularPath.add(p);

                    circularPathes.add(circularPath);
                    break;
                } else {
                    path.push(p);
                    scanDependencies(path);
                    path.pop();
                }
            }
        }
    }

}
