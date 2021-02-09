/*
When a binary operator is in a method call that can receive different number of parameters (e.g., `void info(String var1, Object... var2);`),
we can get an IndexOutOfBoundsException when trying to find the type expected for the argument (i.e., the binary operator in this case).
This test checks that such an exception doesn't happen.
*/

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class NOCOMPILE_BinaryOperatorInMethodCallThatCanReceiveDifferentNumberOfParameters {
    float twoThirds = 2/3; // Noncompliant; THIS LINE WAS ONLY ADDED TO MAKE THE TEST FIND A VIOLATION IN THIS FILE SO THE NEXT LINES CAN BE ACTUALLY TESTED!

    Logger LOGGER = LoggerFactory.getLogger(getClass());

    void method() {
        LOGGER.info("{} {}", 1, 1 + 1); // This is ok, see the method signature: void info(String var1, Object var2, Object var3);
        LOGGER.info("{} {} {}", 1, 2, 1 + 2); // This can cause an IndexOutOfBoundsException, see the method signature: void info(String var1, Object... var2);
    }
}
