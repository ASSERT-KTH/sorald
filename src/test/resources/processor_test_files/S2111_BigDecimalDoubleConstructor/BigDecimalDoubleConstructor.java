// Test for rule s2111

import java.math.BigDecimal;
import java.math.MathContext;

public class BigDecimalDoubleConstructor {

    // Tests from https://rules.sonarsource.com/java/type/Bug/RSPEC-2111
    public void main(String[] args) {
        double d = 1.1;
        BigDecimal bd1 = new BigDecimal(d); // Noncompliant; see comment above
        BigDecimal bd2 = new BigDecimal(1.1); // Noncompliant; same result
    }

    // Tests from https://github.com/SonarSource/sonar-java/blob/master/java-checks-test-sources/src/main/java/checks/BigDecimalDoubleConstructorCheck.java
    public void main2(String[] args) {
        MathContext mc = null;
        BigDecimal bd1 = new BigDecimal("1");
        BigDecimal bd2 = new BigDecimal(2.0); // Noncompliant {{Use "BigDecimal.valueOf" instead.}}
        BigDecimal bd4 = new BigDecimal(2.0, mc); // Noncompliant {{Use "BigDecimal.valueOf" instead.}}
        BigDecimal bd5 = new BigDecimal(2.0f); // Noncompliant {{Use "BigDecimal.valueOf" instead.}}
        BigDecimal bd6 = new BigDecimal(2.0f, mc); // Noncompliant {{Use "BigDecimal.valueOf" instead.}}
        BigDecimal bd3 = BigDecimal.valueOf(2.0);
    }

    // Aditional tests
    public void foo(String[] args) {
        double d = 1.1;
        float f = 2.2f;
        float f1 = 2f;
        BigDecimal bd3 = new BigDecimal(f); // Noncompliant
        BigDecimal bd4 = new BigDecimal(f1); // Noncompliant
        BigDecimal bd5 = BigDecimal.valueOf(d); // Compliant
        BigDecimal bd6 = new BigDecimal("1.1"); // Compliant; using String constructor will result in precise value
        BigDecimal bd7 = BigDecimal.valueOf(f); // Compliant
        BigDecimal bd8 = BigDecimal.valueOf(f1); // Compliant
    }

}
