public class NonFinalPublicStaticField {
    public static Integer meaningOfLife = 42; // Noncompliant
    private static Integer CATCH = 22; // Compliant
    protected static Integer order = 66; // Compliant
    static Integer roadToHill = 30; // Compliant
}