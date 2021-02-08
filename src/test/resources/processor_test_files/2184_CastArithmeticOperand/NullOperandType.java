/*
When we cannot resolve the type of the left or right hand operands in a binary operator, we get a NullPointerException.
This test checks that such an exception doesn't happen.
*/
import javax.jms.Message;

class NullOperandType {
    float twoThirds = 2/3; // Noncompliant; THIS LINE WAS ONLY ADDED TO MAKE THE TEST FIND A VIOLATION IN THIS FILE SO THE NEXT LINE CAN BE ACTUALLY TESTED!

    Message message;
    long var = System.currentTimeMillis() - message.getJMSTimestamp();
}
