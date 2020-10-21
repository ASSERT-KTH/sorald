package sorald;

import java.util.HashMap;
import java.util.Map;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;

/* Only add the CtType object if it does not exist in the map yet */
public class UniqueTypesCollector {
    private static UniqueTypesCollector uniqueTypesCollector;

    private Map<String, CtType> topLevelTypes4Output;

    private UniqueTypesCollector() {
        this.topLevelTypes4Output = new HashMap<String, CtType>();
    }

    public static UniqueTypesCollector getInstance() {
        if (uniqueTypesCollector == null) {
            uniqueTypesCollector = new UniqueTypesCollector();
        }
        return uniqueTypesCollector;
    }

    public Map<String, CtType> getTopLevelTypes4Output() {
        return this.topLevelTypes4Output;
    }

    public void reset() {
        this.uniqueTypesCollector = new UniqueTypesCollector();
    }

    public void collect(CtElement element) {
        if (this.topLevelTypes4Output != null) {
            CtType t = (CtType) element.getParent(CtType.class);
            CtType topParent = t.getReference().getTopLevelType().getDeclaration();
            String filePath = element.getPosition().getFile().getAbsolutePath();

            checkIfThereIsTheSameClassInTheOriginalPath(filePath);
            if (!this.topLevelTypes4Output.containsKey(filePath)) {
                this.topLevelTypes4Output.put(filePath, topParent);
            }
        }
    }

    /*
    When multiple processors are executed, and the first one executed changes a file
    that is also changed by another processor later, the file is registered as changed twice,
    but with different locations (and changes), what leads to the lost of a part of the
    transformations when printing only changed files (FileOutputStrategy.CHANGED_ONLY).
    In such a case, the following method will unregistered an old version of the file.
    */
    private void checkIfThereIsTheSameClassInTheOriginalPath(String filePath) {
        Object topLevelTypeToBeRemoved = null;
        for (Map.Entry topLevelType : this.topLevelTypes4Output.entrySet()) {
            int index = filePath.indexOf(Constants.SPOONED_INTERMEDIATE);
            filePath =
                    ".*"
                            + filePath.substring(
                                    index + Constants.SPOONED_INTERMEDIATE.length(),
                                    filePath.length());
            if (topLevelType.getKey().toString().matches(filePath)) {
                topLevelTypeToBeRemoved = topLevelType.getKey();
                break;
            }
        }
        if (topLevelTypeToBeRemoved != null) {
            this.topLevelTypes4Output.remove(topLevelTypeToBeRemoved);
        }
    }
}
