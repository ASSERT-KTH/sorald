public class JointDeclarationsOfMultipleFields {
    private int a = 1, b = 2, unused1 = 3; // `unused1` is noncompliant
    private char c = 'c', unused2 = 'd', d = 'e'; // `unused2` is noncompliant
    private long unused3 = 1L, e = 2L, f = 3L; // `unused3  ` is noncompliant

    public void printSum() {
        System.out.println(a + b);
        System.out.println(c + d);
        System.out.println(e + f);
    }
}
