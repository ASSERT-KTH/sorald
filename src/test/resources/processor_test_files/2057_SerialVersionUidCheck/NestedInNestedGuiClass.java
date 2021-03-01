/*
Test to ensure that a serializable nested in a GUI class is processed, but the GUI class itself
is not, as it's an exception. The reason for the double nesting here is that Sonar ignores nested
classes if the outermost class is a GUI class. It's unclear whether or not this is intentional
or a bug, but we'll exploit it for the time being for this test case to trigger the desired
code path.
 */

import java.io.Serializable;

public class NestedInNestedGuiClass {
    public static class GuiClass extends java.awt.Canvas implements Serializable {
        public static class Serial implements Serializable { // Noncompliant

        }
    }
}