/**
 * Test for sonarqube rule s2111.
 * From https://sonarqube.ow2.org/coding_rules#rule_key=squid%3AS2111:
 *
 * "The results of this constructor can be somewhat unpredictable. One might assume that writing new BigDecimal(0.1)
 * in Java creates a BigDecimal which is exactly equal to 0.1 (an unscaled value of 1, with a scale of 1),
 * but it is actually equal to 0.1000000000000000055511151231257827021181583404541015625.
 * This is because 0.1 cannot be represented exactly as a double (or, for that matter, as a binary fraction
 * of any finite length). Thus, the value that is being passed in to the constructor is not exactly equal to 0.1,
 * appearances notwithstanding".
 */
import java.math.BigDecimal;

public class BigDecimalDoubleConstructor {

    /*
   Code taken from Sonarqube documentation https://sonarqube.ow2.org/coding_rules#rule_key=squid%3AS2111
    */
    public void foo(String[] args) {
        double d = 1.1;
        float f = 2.2;
        float f1 = 2f;
        BigDecimal bd1 = new BigDecimal(d); // Noncompliant
        BigDecimal bd2 = new BigDecimal(1.1); // Noncompliant
        BigDecimal bd3 = new BigDecimal(f); // Noncompliant
        BigDecimal bd4 = new BigDecimal(f1); // Noncompliant
        BigDecimal bd5 = BigDecimal.valueOf(d); // Compliant
        BigDecimal bd6 = new BigDecimal("1.1"); // Compliant; using String constructor will result in precise value
        BigDecimal bd7 = BigDecimal.valueOf(f); // Compliant
        BigDecimal bd8 = BigDecimal.valueOf(f1); // Compliant
    }

}
