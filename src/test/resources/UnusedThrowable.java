// Test for rule s3984
// Tests from https://github.com/SonarSource/sonar-java/blob/master/java-checks/src/test/files/checks/unused/UnusedThrowableCheck.java

class UnusedThrowable {
	void foo(int x) {
		if (x < 0) {
			new IllegalArgumentException("x must be nonnegative"); // Noncompliant {{Throw this exception or remove this useless statement}}
		}
		if (x < 0) {
			throw new IllegalArgumentException("x must be nonnegative");
		}
		new UnusedThrowable();
		Throwable t = new IllegalArgumentException("x must be nonnegative");
		if (x < 0) {
			throw t;
		}
	}
}