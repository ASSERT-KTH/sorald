// Test for rule s2164

class MathOnFloat {

    float e = 2.71f;
    float pi = 3.14f;
    double c = e * pi; // Noncompliant

    // Tests from https://github.com/SonarSource/sonar-java/blob/master/java-checks-test-sources/src/main/java/checks/MathOnFloatCheck.java
    void myMethod() {
        float a = 16777216.0f;
        float b = 1.0f;

        double d1 = a + b; // Noncompliant ; addition is still between 2 floats
        double d2 = a - b; // Noncompliant
        double d3 = a * b; // Noncompliant
        double d4 = a / b; // Noncompliant
        double d5 = a / b + b; // Noncompliant

        double d6 = a + d1;

        double d7 = a + a / b; // Noncompliant

        int i = 16777216;
        int j = 1;
        int k = i + j;
        System.out.println("[Max time to retrieve connection:"+(a/1000f/1000f)+" ms.");
        System.out.println("[Max time to retrieve connection:"+a/1000f/1000f);
    }

}
