public class UtilityClassWithImplicitPublicConstructor { // Noncompliant
    public static final String OFTEN_MISQUOTED_QUOTE = "No, I am your father";

    public static final String getQuote() {
        return OFTEN_MISQUOTED_QUOTE;
    }
}