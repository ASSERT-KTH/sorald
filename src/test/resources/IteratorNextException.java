// Test for rule s2272

import java.util.Iterator;
import java.util.Stack;

// Test based on https://rules.sonarsource.com/java/type/Bug/RSPEC-2272
public class IteratorNextException implements Iterator {

    private Stack<String> stack = new Stack();

    @Override
    public boolean hasNext() {
        return !stack.empty();
    }

    @Override
    public String next() { // Noncompliant
        if (!hasNext()) {
            return null;
        } else {
            return stack.pop();
        }
    }

}
