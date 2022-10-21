public class PrivateFieldWrittenButNotRead {
    private int x; // Noncompliant

    private String foo; // Noncompliant

    private long used;

    public PrivateFieldWrittenButNotRead(int x, int y) {
        this.x = x + y;
    }

    public void initialize(String bar) {
        foo = bar;
        used = 234324L;
    }

    public long getUsed() {
        return used;
    }
}
