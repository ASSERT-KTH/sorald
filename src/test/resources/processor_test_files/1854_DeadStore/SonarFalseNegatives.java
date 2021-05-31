/*
This test file contains cases that are in fact dead stores, but aren't considered as such
by Sonar at the time of writing.
 */

public class SonarFalseNegatives {

    // method with a dead store just to make this test case run
    public void deadStore() {
        int x = 2; // Noncompliant
    }

    public void forUpdate() {
        int x = 1;
        for (int y = 0; y < 10; x++) { // Compliant; dead store in x, false negative by sonar
            y++;
        }
    }

    void expressionPlusEquals() {
        int x = 1;
        int y = (x += 10); // Compliant; dead store in x, false negative by sonar
        System.out.println(y);
    }
}