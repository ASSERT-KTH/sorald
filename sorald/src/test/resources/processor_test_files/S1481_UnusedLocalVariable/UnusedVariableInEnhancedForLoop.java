import java.util.List;

public class UnusedVariableInEnhancedForLoop {
    public void inForLoop(List<String> inputList) {
        // Unused local variable in for loop header
        // should be removed
        for (String input : inputList) { // noncompliant
            doSomething();
        }
        for (String input : inputList) { // noncompliant
            for (String input2 : inputList) { // noncompliant
                doSomething();
            }
        }
        int[] array = new int[10];
        for (int i : array) { // noncompliant
            doSomething();
        }
    }

    public void doSomething() { }

}