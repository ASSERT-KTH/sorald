import org.sonar.java.AnalyzerMessage;

import java.util.HashSet;
import java.util.Set;

public class Bug
{
    private AnalyzerMessage message;
    private long lineNumber;
    private String name;
    private String fileName;
    public Bug() {

    }

    public Bug(Bug bug){
        this.message=bug.message;
        init();
    }
    public Bug(AnalyzerMessage message)
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
        if(!(obj instanceof Bug))
        {
            return false;
        }
        if(obj==this)
        {
            return true;
        }
        Bug rhs=(Bug) obj;
        return rhs.message.toString().equals(this.message.toString());
    }

    public static Set<Bug> createSetOfBugs(Set<AnalyzerMessage> total){
        Set<Bug> setOfBugs = new HashSet<>();

        for (AnalyzerMessage message : total) {
            Bug bug = new Bug(message);
            setOfBugs.add(bug);
        }
        return setOfBugs;
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
