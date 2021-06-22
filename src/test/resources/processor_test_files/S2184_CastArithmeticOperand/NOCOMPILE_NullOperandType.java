/*
When we cannot resolve the type of the left or right hand operands in a binary operator, we get a NullPointerException.
This test checks that such an exception doesn't happen.
*/
class NOCOMPILE_NullOperandType {
    float twoThirds = 2/3; // Noncompliant; THIS LINE WAS ONLY ADDED TO MAKE THE TEST FIND A VIOLATION IN THIS FILE SO THE NEXT LINE CAN BE ACTUALLY TESTED!
    long rhsUnknown = System.currentTimeMillis() - message.getJMSTimestamp();
    long lhsUnknown = message.getJMSTimestamp() - System.currentTimeMillis();
}
