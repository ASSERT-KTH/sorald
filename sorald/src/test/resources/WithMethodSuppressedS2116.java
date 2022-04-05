public class WithMethodSuppressedS2116 {

    @SuppressWarnings("squid:S2116")
    public static void main(String[] args) {
        String argStr = args.toString(); // Noncompliant (rule S2116)
        float f = 1.0f + 2.0f; // Noncompliant (rule S2164)
    }
}