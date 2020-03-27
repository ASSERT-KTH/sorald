package sonarquberepair.processor.sonarbased;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtElement;

public abstract class SonarWebAPIBasedProcessor<E extends CtElement> extends AbstractProcessor<E> {

	protected JSONArray jsonArray; //array of JSONObjects, each of which is a bug
	protected Set<Bug> setOfBugs; //set of bugs, corresponding to jsonArray
	protected Set<Long> setOfLineNumbers; //set of line numbers corresponding to bugs, just for efficiency
	protected Set<String> setOfFileNames; //-----
	protected Bug thisBug;               //current bug. This is set inside isToBeProcessed function
	protected String thisBugName;        //name (message) of current thisBug.

	SonarWebAPIBasedProcessor(int ruleKey, String projectKey) {
		try {
			jsonArray = this.parse(ruleKey, "", projectKey);
			setOfBugs = Bug.createSetOfBugs(this.jsonArray);
			setOfLineNumbers = new HashSet<Long>();
			setOfFileNames = new HashSet<String>();
			thisBug = new Bug();
			for (Bug bug : setOfBugs) {
				setOfLineNumbers.add(bug.getLineNumber());
				setOfFileNames.add(bug.getFileName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	parse(int,String) makes a GET request and parses the returned JSONObject.
	Pass the ruleKey as the first parameter to parse. If no particular rule is required, pass 0 to it.
	The second parameter is the specific file or directory in spoon you want to get the bugs for.
	Example: fName="src/main/java/spoon/MavenLauncher.java"; If you want to parse the entire source code, you need to pass "src/" in fname
	 */
	private JSONArray parse(int ruleKey, String fName, String projectKey) throws Exception {
		String ruleParameter = "&rules=squid:S" + Integer.toString(ruleKey);
		String url = "";
		if (projectKey.equals("fr.inria.gforge.spoon:spoon-core")) {
			url = "https://sonarqube.ow2.org/api/issues/search?resolved=false&ps=500" + ruleParameter + "&componentKeys=" + projectKey;
		} else {
			url = "https://sonarcloud.io/api/issues/search?resolved=false&ps=500" + ruleParameter + "&componentKeys=" + projectKey;
		}
		if (fName.length() > 0) {
			url = url + ":" + fName;
		}
		URL url1 = new URL(url);
		HttpURLConnection con = (HttpURLConnection) url1.openConnection();
		con.setRequestMethod("GET");
		int responseCode = con.getResponseCode();
		System.out.println("\nSending GET request to SonarQube API : " + url);
		if (responseCode != 200) {
			System.out.println("Response Code : " + responseCode);
			throw new Exception("ERROR : Wrong Reponse Code from Sonarqube API. Check Internet Connection");
		}
		System.out.println("GET Request Successful");
		System.out.println();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		JSONObject jo = new JSONObject(response.toString());
		JSONArray jsonArray = jo.getJSONArray("issues");
		if (jsonArray.length() == 0) {
			System.out.println("No Sonarqube issues found. Maybe you entered wrong project key.");
			System.out.println("Here is the JSON response from Sonarqube:");
			System.out.println(jo.toString());
		}
		if (projectKey.equals("fr.inria.gforge.spoon:spoon-core")) {
			System.out.println(jsonArray.toString());
		}
		return jsonArray;
	}

}
