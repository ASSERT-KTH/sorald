public class PrivateFieldWrittenButNotRead {
    private int x; // Noncompliant

    private String foo; // Noncompliant

    private long zhang;

    public PrivateFieldWrittenButNotRead(int x, int y) {
        this.x = x + y;
    }

    public void initialize(String bar) {
        foo = bar;
        zhang = 234324L;
    }

    public long getZhang() {
        return zhang;
    }
}
