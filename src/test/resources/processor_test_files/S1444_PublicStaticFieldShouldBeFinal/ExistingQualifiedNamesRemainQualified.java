// this test file really has little to do with rule 1444, it's just a little hack to check
// that Sorald's import processor does not try to import types that are specified with fully
// qualifieg names in the original input file

public class ExistingQualifiedNamesRemainQualified {
    public static String s = "some value"; // Noncompliant

    public static void main(String[] args) {
        // now this is what we're actually testing, if Sorald is too aggresive in processing types
        // for auto-importing, java.util.ArrayList and javax.xml.XMLConstants will be imported
        java.util.ArrayList<Integer> list = new java.util.ArrayList<>();
        String constant = javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD;
    }
}
