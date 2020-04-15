package sonarquberepair;

import java.util.HashMap;
import java.util.Map;

/* this is used to look up the link of the corresponding rule on SonarQube website */
public class RuleLinkDictionary {
	private static RuleLinkDictionary RuleLinkDictionary;
	Map<String,String> dictionary = new HashMap<String,String>();

	private RuleLinkDictionary() {
		dictionary.put();
		dictionary.put();
		dictionary.put();
		dictionary.put();
		dictionary.put();
	}

	public static RuleLinkDictionary getInstance() {
		if (RuleLinkDictionary == null) {
			RuleLinkDictionary = new RuleLinkDictionary();
		}
		return RuleLinkDictionary;
	}

	public Map<String,String> getDictionary() {
		return this.dictionary;
	}
}