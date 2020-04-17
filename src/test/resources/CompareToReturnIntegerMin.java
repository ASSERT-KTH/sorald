package apps;

public class CompareToReturnIntegerMin {

	public int compareTo(boolean cond) {
		if (cond) {
    		return Integer.MIN_VALUE;  // Noncompliant
    	}
    }
}