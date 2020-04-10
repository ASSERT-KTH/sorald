package sonarquberepair;

import java.util.HashMap;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtElement;

/* Only add the CtType object if it does not exist in the map yet */
public class UniqueTypesCollector {
	private static UniqueTypesCollector uniqueTypesCollector;

	private HashMap<Integer,CtType> topLevelTypes4Output = new HashMap<Integer,CtType>();

	private UniqueTypesCollector() {}

	public static UniqueTypesCollector getInstance() {
		if (uniqueTypesCollector == null) {
			uniqueTypesCollector = new UniqueTypesCollector();
		}
		return uniqueTypesCollector;
	}

	public HashMap<Integer,CtType> getTopLevelTypes4Output() {
		return this.topLevelTypes4Output;
	}

	public void reset() {
		this.uniqueTypesCollector = new UniqueTypesCollector();
	}

	public void findAndAddTopTypeOf(CtElement element) {
		if (this.topLevelTypes4Output != null) {
			CtType t = (CtType)element.getParent(CtType.class);
			CtType topParent = t.getReference().getTopLevelType().getDeclaration();
			Integer hashCode = new Integer(topParent.hashCode());

			if (!this.topLevelTypes4Output.containsKey(hashCode)) {
				this.topLevelTypes4Output.put(hashCode,topParent);
			}
		}
	}
}