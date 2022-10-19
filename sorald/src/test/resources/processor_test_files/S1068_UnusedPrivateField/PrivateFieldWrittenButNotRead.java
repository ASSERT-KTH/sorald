public class PrivateFieldWrittenButNotRead {
    private int x; // Noncompliant

    private String foo; // Noncompliant

    public PrivateFieldWrittenButNotRead(int x, int y) {
        this.x = x + y;
    }

    public void initialize(String bar) {
        foo = bar;
    }
}
