package sorald;

import java.util.Random;

public class NullReturningToString {
    @Override
    public String toString() {
        Random r = new Random();
        if(r.nextInt(10) == r.nextInt(10)){
            return ""; // Noncompliant
        }
        else if(r.nextInt(10) == r.nextInt(10)){
            return "null";
        }
        return "";
    }
}
