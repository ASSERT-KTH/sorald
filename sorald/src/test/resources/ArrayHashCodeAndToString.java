// Test for rule s2116

public class ArrayHashCodeAndToString {

	// Tests from https://rules.sonarsource.com/java/type/Bug/RSPEC-2116
	public static void main( String[] args ) {
		String argStr = args.toString(); // Noncompliant
		int argHash = args.hashCode(); // Noncompliant
	}

	// Tests from https://github.com/SonarSource/sonar-java/blob/master/java-checks-test-sources/src/main/java/checks/ArrayHashCodeAndToStringCheck.java
	void method(String[] args, String string) {
		Class class1 = args.getClass();
		String str = string.toString();
		int hash = string.hashCode();
	}

	// Aditional tests
	public void foo(String[] args) {
		String[] array1 = {"F", "O", "O"};
		System.out.println(array1.toString()); // Noncompliant
		varargsTest(1, 2, 3);
	}

	private void varargsTest(int... array2) {
		String a = array2.toString(); // Noncompliant
	}

}
