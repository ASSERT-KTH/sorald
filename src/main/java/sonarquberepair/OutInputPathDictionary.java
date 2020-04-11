package sonarquberepair;

import java.util.HashMap;
import java.util.Map;

/* this is used to look up where the original file was given an output file by SonarQube repair */
public class OutInputPathDictionary {
	private static OutInputPathDictionary outInputPathDictionary;
	Map<String,String> dictionary = new HashMap<String,String>();

	private OutInputPathDictionary() {}

	public static OutInputPathDictionary getInstance() {
		if (outInputPathDictionary == null) {
			outInputPathDictionary = new OutInputPathDictionary();
		}
		return outInputPathDictionary;
	}

	public void put(String outputAbsPath,String inputAbsPath) {
		this.dictionary.put(outputAbsPath,inputAbsPath);
	}

	public Map<String,String> getDictionary() {
		return this.dictionary;
	}
}