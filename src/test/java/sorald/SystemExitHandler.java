package sorald;

import java.security.Permission;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

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
