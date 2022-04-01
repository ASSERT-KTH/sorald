// Test for rule s2116

public class NOSONARCommentTest {

	// Tests from https://rules.sonarsource.com/java/type/Bug/RSPEC-2116
	public static void main( String[] args ) {
		String argStr = args.toString(); // Noncompliant, NOSONAR
		int argHash = args.hashCode(); // Noncompliant
	}

}
