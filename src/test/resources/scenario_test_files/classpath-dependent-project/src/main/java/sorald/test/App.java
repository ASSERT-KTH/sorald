package sorald.test;

/**
 * App that computes the ratio of root operations to all opertions with gumtree-spoon!
 */
public class App {
    public static void main(String[] args) {
        Diff diff = new AstComparator().compare(leftPath.toFile(), rightPath.toFile());
        double ratio = diff.getRootOperations().size() / diff.getAllOperations().size(); // Noncompliant; rule 2184 (cast arithmetic operator)
        System.out.println(ratio);
    }
}
