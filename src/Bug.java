import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class Bug
{

    private JSONObject jsonObject;
    private long lineNumber;
    private String name;
    private String fileName;
    public Bug() throws Exception {
//        throw new Exception("ERROR : Please pass JsonObject to constructor of Bug");
    }

    public Bug(JSONObject jsonObject) throws JSONException {
        this.jsonObject=jsonObject;
        init();
    }
    public Bug(Bug bug) throws JSONException {
        this.jsonObject=bug.jsonObject;
        init();
    }
    private void init() throws JSONException
    {
        lineNumber=(long)(int)jsonObject.get("line");//cast first to int then to long
        name=(String) jsonObject.get("message");
        String split[]=jsonObject.get("component").toString().split("/");
        fileName=split[split.length-1];
    }

    @Override
    public int hashCode()
    {
        return this.jsonObject.toString().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof Bug))
        {
            return false;
        }
        if(obj==this)
        {
            return true;
        }
        Bug rhs=(Bug) obj;
        return rhs.jsonObject.toString().equals(this.jsonObject.toString());
    }

    public static Set<Bug> createSetOfBugs(JSONArray jsonArray) throws Exception {
        Set<Bug> SetOfBugs = new HashSet<Bug>();
        if (jsonArray == null) {
            throw new Exception("null JSONArray passed to createSetOfBugs()");
        }
        for (int i = 0; i < jsonArray.length(); ++i) {
            JSONObject obj = null;
            try {
                obj = jsonArray.getJSONObject(i);
                Bug bug = new Bug(obj);
                SetOfBugs.add(bug);
                /*
                System.out.println(obj);
                System.out.println(obj.get("line"));
                System.out.println(obj.get("message"));
                System.out.println("\n\n\n");
                */
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return SetOfBugs;
    }

    public String getName() {
        return name;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public String getFileName() {
        return fileName;
    }
}

