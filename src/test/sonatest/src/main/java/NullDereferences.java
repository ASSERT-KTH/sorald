import java.io.*;

public class NullDereferences
{

    public void Ndrf()
    {
        String str=null,a=null,b=null;
        if(a == null && (b != null || b.length()>0)){} // Noncompliant
        if((str) == null && str.length() == 0){} 
        if((str == null) && str.length() == 0){}
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
            return Integer.parseInt(extractVariable(javaVersion).substring(2));// Noncompliant
        }
        javaVersion = getProperty("java.src.version");
        if (javaVersion != null) {
            return Integer.parseInt(extractVariable(javaVersion).substring(2));// Noncompliant
        }
        javaVersion = getProperty("maven.compiler.source");
        if (javaVersion != null) {
            return Integer.parseInt(extractVariable(javaVersion).substring(2));// Noncompliant
        }
        javaVersion = getProperty("maven.compile.source");
        if (javaVersion != null) {
            return Integer.parseInt(extractVariable(javaVersion).substring(2));// Noncompliant
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

