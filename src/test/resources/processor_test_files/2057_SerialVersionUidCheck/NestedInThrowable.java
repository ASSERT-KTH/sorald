/*
Test to ensure that enclosing class that extends throwable is not considered for processing,
as it's an exception from the rule.
 */

import java.io.Serializable;

public class NestedInThrowable extends Throwable implements Serializable {
    public static class Serial implements Serializable { // Noncompliant

    }
}