package sonarquberepair.processor.sonarbased;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class Bug {

	private JSONObject jsonObject;
	private JSONArray locations;
	private String ruleName;
	private int lineNumber;
	private String name;
	private String fileName;

	public Bug() {}

	public Bug(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
		init();
	}

	public Bug(Bug bug) {
		this.jsonObject = bug.jsonObject;
		init();
	}

	private void init() {
		JSONArray flow = jsonObject.getJSONArray("flows");
		if (flow.length() > 0) {
			locations = flow.getJSONObject(0).getJSONArray("locations");
		}
		ruleName = (String) jsonObject.get("rule");
		if (jsonObject.has("line")) {
			lineNumber = (int) (jsonObject.get("line"));
		}
		name = (String) jsonObject.get("message");
		String[] split = jsonObject.get("component").toString().split("/");
		fileName = split[split.length - 1];
	}

	@Override
	public int hashCode() {
		return this.jsonObject.toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Bug)) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		Bug rhs = (Bug) obj;
		return rhs.jsonObject.toString().equals(this.jsonObject.toString());
	}

	public static Set<Bug> createSetOfBugs(JSONArray jsonArray) throws Exception {
		Set<Bug> setOfBugs = new HashSet<>();
		if (jsonArray == null) {
			throw new Exception("null JSONArray passed to createSetOfBugs()");
		}
		for (int i = 0; i < jsonArray.length(); ++i) {
			JSONObject obj = null;
			try {
				obj = jsonArray.getJSONObject(i);
				Bug bug = new Bug(obj);
				setOfBugs.add(bug);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return setOfBugs;
	}

	public JSONObject getJsonObject() {
		return jsonObject;
	}

	public JSONArray getLocations() {
		return locations;
	}

	public String getName() {
		return name;
	}

	public int getRuleKey() {
		return Integer.parseInt(ruleName.replace("java:S", ""));
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public String getFileName() {
		return fileName;
	}

	public void printBugLocations() {
		try {
			if (locations != null) {
				for (int i = 0; i < locations.length(); ++i) {
					System.out.println(locations.getJSONObject(i));
				}
			} else {
				System.out.println("null locations");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
