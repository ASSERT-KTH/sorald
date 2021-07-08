public class JointDeclarationOfTwoFields {
    private int unusedInt = 2, usedInt = 1; // Noncompliant and compliant respectively
    private String usedStr = "hello", unusedStr = "world"; // Compliant and noncompliant respectively

    public void printSomething() {
        System.out.println(usedInt);
        System.out.println(usedStr);
    }
}
