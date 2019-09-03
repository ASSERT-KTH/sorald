/**
 * Test for sonarqube rule s4973
 * Boxed types should be compared with equals() rather than "==" since equals compares values while
 * "==" compares memory location.
 */

public class BoxedTypesEquals {

    boolean eq = true;

    // Java implicitly converts one variable to primitive if something boxed and primitive is compared.
    private void mixedCompare(){
        int e = 4;
        Integer f = 4;
        eq = (e == f);// Compliant;
    }

    // Integer is not primitive and should use .equals()
    private boolean IntegerCompare(){
        Integer a = 5;
        Integer b = 5;
        return b == a;// Noncompliant
    }

    // Int is primitive and can use ==
    private void intCompare(){
        int x = 5;
        int y = 5;
        eq = (x == y); // Compliant;
    }

    // String is not primitive and should use .equals()
    private void stringCompare(){
        String firstName = getFirstName(); // String overrides equals
        String lastName = getLastName();
        eq = (firstName == lastName);// Noncompliant
    }

    private String getFirstName(){
        return new String("John");
    }

    private String getLastName(){
        return new String("John");
    }
}
