/**
 * Test for sonarqube rule s2272
 * By contract, any implementation of Iterator.next() should throw a NoSuchElementException when there are no more elements.
 */
import java.util.Iterator;
import java.util.Stack;

public class NoSuchElement implements Iterator<String> {
    private Stack<String> stack = new Stack();

    @Override
    public boolean hasNext() {
        return !stack.empty();
    }

    @Override
    public String next(){ // Noncompliant; Should throw a NoSuchElementException rather than return null
        if(!hasNext()){
            return null;
        }
        return stack.pop();
    }
}
