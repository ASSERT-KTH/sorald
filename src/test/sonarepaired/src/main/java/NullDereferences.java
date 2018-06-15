import java.io.*;

public class NullDereferences
{

    public void Ndrf()
    {
        String str=null,a=null,b=null;
        if(a == null && (b != null || b.length()>0)){} // Noncompliant {{Either reverse the equality operator in the "b" null test, or reverse the logical operator that follows it.}}
        if((str) == null && str.length() == 0){} // Noncompliant
        if((str == null) && str.length() == 0){} // Noncompliant
    }
    private String extractVariable(String value) {
        if (value != null && value.startsWith("$")) {
            value = getProperty(value.substring(2, value.length() - 1));
        }
        return value;
    }
    int getSourceVersion() {
        String javaVersion = getProperty("java.version");
        if (javaVersion != null) {
            return Integer.parseInt(extractVariable(javaVersion).substring(2));
        }
        javaVersion = getProperty("java.src.version");
        if (javaVersion != null) {
            return Integer.parseInt(extractVariable(javaVersion).substring(2));
        }
        javaVersion = getProperty("maven.compiler.source");
        if (javaVersion != null) {
            return Integer.parseInt(extractVariable(javaVersion).substring(2));
        }
        javaVersion = getProperty("maven.compile.source");
        if (javaVersion != null) {
            return Integer.parseInt(extractVariable(javaVersion).substring(2));
        }
        // return the current compliance level of spoon
        int x=5;
        return x*x;
    }
    private String getProperty(String key) {
        String value = extractVariable("hello");
        if (value == null) {
            return null;
        }
        return value;
    }


}

