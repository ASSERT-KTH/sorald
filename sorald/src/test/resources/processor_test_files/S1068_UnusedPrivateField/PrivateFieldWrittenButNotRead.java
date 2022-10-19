public class PrivateFieldWrittenButNotRead {
    private int x;

    private String foo;

    public PrivateFieldWrittenButNotRead(int x, int y) {
        this.x = x + y;
    }

    public void initialize(String bar) {
        foo = bar;
    }
}
