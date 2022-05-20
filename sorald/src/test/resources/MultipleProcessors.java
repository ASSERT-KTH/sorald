import java.math.BigDecimal;

import java.util.concurrent.atomic.AtomicInteger;

class MultipleProcessors {

	void foo() {
		BigDecimal bd2 = new BigDecimal(1.1); // Noncompliant by BigDecimalDoubleConstructorCheck

		float twoThirds = 2/3; // Noncompliant by CastArithmeticOperandCheck

		AtomicInteger aInt1 = new AtomicInteger(0);
		AtomicInteger aInt2 = new AtomicInteger(0);
		boolean isEqual = aInt1.equals(aInt2); // Noncompliant by EqualsOnAtomicClassCheck
	}

}
