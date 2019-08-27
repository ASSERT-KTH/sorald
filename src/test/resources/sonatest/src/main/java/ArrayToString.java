/**
 * Test for sonarqube rule s2116
 * Arrays should not have their .toString method called as this will return the reference which is almost always wrong.
 * Instead, Arrays.toString(ARRAY_VARIABLE) should be used.
 */

public class ArrayToString {

    public void foo() {
        String[] array1 = {"F", "O", "O"};
        System.out.println(array1.toString());// Noncompliant
        varargsTest(1, 2, 3);
    }

    private void varargsTest(int ... array2){
        String a = array2.toString();// Noncompliant
    }
}
