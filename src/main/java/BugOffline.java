import org.sonar.java.AnalyzerMessage;

import java.util.HashSet;
import java.util.Set;

public class BugOffline
{
    private AnalyzerMessage message;
    private long lineNumber;
    private String name;
    private String fileName;
    public BugOffline() {

    }

    public BugOffline(BugOffline BugOffline){
        this.message=BugOffline.message;
        init();
    }
    public BugOffline(AnalyzerMessage message)
    {
        this.message = message;
        init();
    }
    private void init()
    {
        this.lineNumber=message.getLine();
        this.name=message.getMessage();
        this.fileName=message.getFile().getName();
    }

    @Override
    public int hashCode()
    {
        return this.message.toString().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof BugOffline))
        {
            return false;
        }
        if(obj==this)
        {
            return true;
        }
        BugOffline rhs=(BugOffline) obj;
        return rhs.message.toString().equals(this.message.toString());
    }

    public static Set<BugOffline> createSetOfBugOfflines(Set<AnalyzerMessage> total){
        Set<BugOffline> setOfBugOfflines = new HashSet<>();

        for (AnalyzerMessage message : total) {
            BugOffline BugOffline = new BugOffline(message);
            setOfBugOfflines.add(BugOffline);
        }
        return setOfBugOfflines;
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

    public AnalyzerMessage getMessage()
    {
        return message;
    }

}
