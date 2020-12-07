package sorald;

import java.security.Permission;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

/**
 * Handler for calls to {@link System#exit(int)}. It intercepts any calls and throws a {@link
 * NonZeroExit} whenever a non-zero exit is encountered.
 */
public class SystemExitHandler implements TestExecutionListener {

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        System.setSecurityManager(
                new SecurityManager() {
                    @Override
                    public void checkPermission(Permission perm) {}

                    @Override
                    public void checkPermission(Permission perm, Object context) {}

                    @Override
                    public void checkExit(int status) {
                        if (status != 0) {
                            throw new NonZeroExit();
                        }
                    }
                });
    }

    @Override
    public void executionFinished(
            TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        System.setSecurityManager(null);
    }

    public static class NonZeroExit extends RuntimeException {}
}
