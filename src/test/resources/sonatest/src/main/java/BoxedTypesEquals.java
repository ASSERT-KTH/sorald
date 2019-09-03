/**
 * Test for sonarqube rule s4973
 * Boxed types should be compared with equals() rather than "==" since equals compares values while
 * "==" compares memory location.
 */

public class BoxedTypesEquals {

    private String getFirstName(){
        return new String("John");
    }

    private String getLastName(){
        return new String("John");
    }

    String firstName = getFirstName(); // String overrides equals
    String lastName = getLastName();
    boolean x = (firstName == lastName);// Noncompliant
}
