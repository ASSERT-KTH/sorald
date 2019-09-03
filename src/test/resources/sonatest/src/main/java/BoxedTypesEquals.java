/**
 * Test for sonarqube rule s4973
 * Boxed types should be compared with equals() rather than "==" since equals compares values while
 * "==" compares memory location.
 */

public class BoxedTypesEquals {

    public void foo(){
        String firstName = getFirstName(); // String overrides equals
        String lastName = getLastName();
        boolean eq = (firstName == lastName);// Noncompliant
        int x = 5;
        int y = 5;
        eq = (x == y); // Compliant; Non-boxed variables
        /*
        Add a custom type equality check which overrides the equals() method
         */
    }

    private String getFirstName(){
        return new String("John");
    }

    private String getLastName(){
        return new String("John");
    }
}
