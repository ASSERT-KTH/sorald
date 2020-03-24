/**
 * Test for sonarqube rule s2272
 * By contract, any implementation of Iterator.next() should throw a NoSuchElementException when there are no more elements.
 */
import java.util.Iterator;
import java.util.Stack;

public class IteratorNextException implements Iterator {
    private Stack<String> stack = new Stack();

    @Override
    public boolean hasNext() {
        return !stack.empty();
    }

    @Override
    public String next(){ // Noncompliant
        if(!hasNext()){
            return null;
        }else{
            return stack.pop();
        }
    }
}
