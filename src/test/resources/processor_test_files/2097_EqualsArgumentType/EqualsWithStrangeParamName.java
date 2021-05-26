/*
Test that the processor dynamically resolves the parameter name.
 */

public class EqualsWithStrangeParamName {
    private int x = 22;

    @Override
    public boolean equals(Object someStrangeParameterName) { // Noncompliant
        return this.x == ((EqualsWithStrangeParamName) someStrangeParameterName).x;
    }
}