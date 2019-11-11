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
        eq = (e != f);// Compliant;
        eq = (f == e);// Compliant;
    }

    // Integer is not primitive and should use .equals()
    private boolean IntegerCompare(){
        Integer a = 5;
        Integer b = 5;
        return b != a;// Noncompliant
    }

    // Int is primitive and can use ==
    private void intCompare(){
        int x = 5;
        int y = 5;
        eq = (x == y); // Compliant;
        eq = (y == x); // Compliant;
    }

    // Null comparisons are excluded from transformation
    private void nullCompare(){
        String x = null;
        eq = (x == null); // Compliant
        eq = (null == x); // Compliant
    }

    // ENUM comparisons are excluded from transformation
    private void nullCompare(){
        enum foo {
            BAR,
            XOR
        }
        foo x = foo.BAR;
        eq = (x == foo.BAR); // Compliant
        eq = (foo.XOR == x); // Compliant
    }

    // String is not primitive and should use .equals()
    private boolean stringCompare(){
        String firstName = getFirstName(); // String overrides equals
        String lastName = getLastName();
        if(firstName == lastName){// Noncompliant
            return true;
        }
        return false;
    }

    // Object comparison should not be converted
    private void objectCompare(){
        Object a = 1;
        Object b = 1;
        eq = a == b; // Compliant
        int x = 2;
        eq = a == x; // Compliant
    }

    private String getFirstName(){
        return new String("John");
    }

    private String getLastName(){
        return new String("John");
    }
}
