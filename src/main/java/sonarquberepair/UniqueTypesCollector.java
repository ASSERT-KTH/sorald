package sonarquberepair;

import java.util.HashMap;
import java.util.Map;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtElement;

/* Only add the CtType object if it does not exist in the map yet */
public class UniqueTypesCollector {
	private static UniqueTypesCollector uniqueTypesCollector;

	private Map<String,CtType> topLevelTypes4Output = new HashMap<String,CtType>();

	private UniqueTypesCollector() {}

	public static UniqueTypesCollector getInstance() {
		if (uniqueTypesCollector == null) {
			uniqueTypesCollector = new UniqueTypesCollector();
		}
		return uniqueTypesCollector;
	}

	public Map<String,CtType> getTopLevelTypes4Output() {
		return this.topLevelTypes4Output;
	}

	public void reset() {
		this.uniqueTypesCollector = new UniqueTypesCollector();
	}

	@Override
	public void collect(CtElement element) {
		if (this.topLevelTypes4Output != null) {
			CtType t = (CtType)element.getParent(CtType.class);
			CtType topParent = t.getReference().getTopLevelType().getDeclaration();
			String originalFilePath = element.getPosition().getFile().getAbsolutePath();

			if (!this.topLevelTypes4Output.containsKey(originalFilePath)) {
				this.topLevelTypes4Output.put(originalFilePath,topParent);
			}
		}
	}
}