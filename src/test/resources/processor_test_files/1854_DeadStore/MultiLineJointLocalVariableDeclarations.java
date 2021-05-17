/*
An assortment of multiline joint local variable declarations.
 */

public class MultiLineJointLocalVariableDeclarations {

    public void multiLineJointDeclarationWithDeadStore() {
        int veryLongVariableName = 1,
            otherLongVariableName = 2,
            finalLongVariableName = 3; // Noncompliant
        System.out.println(veryLongVariableName);
        System.out.println(otherLongVariableName);
    }

    public void multiLineJointDeclarationWithMissingInitializer() {
        int veryLongVariableName = 1,
                otherLongVariableName,
                finalLongVariableName = 3; // Noncompliant
        System.out.println(veryLongVariableName);
        otherLongVariableName = 2;
        System.out.println(otherLongVariableName);
    }
}