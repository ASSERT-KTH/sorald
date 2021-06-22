// Test for rule s2142
// From https://github.com/SonarSource/sonar-java/blob/971eeaf972d8112aa4b7dd9cbf0ed577b397d6a7/java-checks/src/test/files/checks/InterruptedExceptionCheck.java
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.FutureTask;

enum Level {
	WARN;
}
interface Log {
	void log(Level level, String s, Exception e);
}

class InterruptedExceptionForTesting {
	static final Log LOGGER = null;

	public void run () {
		try {
			while (true) {
				// do stuff
				throw new InterruptedException();
			}
		}catch (InterruptedException e) { // Noncompliant; logging is not enough
			LOGGER.log(Level.WARN, "Interrupted!", e);
		}
	}

	public void runUnknownSymbol () {
		try {
			if (1 < 2) {
				throw new InterruptedException();
			} else {
				throw new IOException();
			}
		}catch (IOException e) {
			LOGGER.log(Level.WARN, "Interrupted!", e);
		}catch (InterruptedException e) { // Noncompliant
		}
	}

	public void catchUnionType () {
		try {
			if (1 < 2) {
				throw new InterruptedException();
			} else {
				throw new IOException();
			}
		} catch (InterruptedException | IOException e) { // Noncompliant {{Either re-interrupt this method or rethrow the "InterruptedException".}}
		}
	}

	public void run2 () throws InterruptedException, IOException {
		try {
			while (true) {
				// do stuff
				throw new InterruptedException();
			}
		} catch (InterruptedException e) {
			LOGGER.log(Level.WARN, "Interrupted!", e);
			// clean up state...
			throw e;
		} catch (ThreadDeath threadDeath) { // Noncompliant {{Either re-interrupt this method or rethrow the "ThreadDeath".}}
			throw new IOException();
		}
	}

	public void run3 () {
		try {
			while (true) {
				// do stuff
				throw new InterruptedException();
			}
		} catch (InterruptedException e) {
			LOGGER.log(Level.WARN, "Interrupted!", e);
			// clean up state...
			Thread.currentThread().interrupt();
		}
		try {
			while (true) {
				// do stuff
				throw new InterruptedException();
			}
		} catch (InterruptedException e) { // Noncompliant {{Either re-interrupt this method or rethrow the "InterruptedException".}}
			LOGGER.log(Level.WARN, "Interrupted!", e);
			// clean up state...
			new Foo().interrupt();
		}
	}

	public FutureTask getNextTask(BlockingQueue<FutureTask> queue) {
		boolean interrupted = false;
		try {
			while (true) {
				try {
					return queue.take();
				} catch (InterruptedException e) {
					interrupted = true;
					// fall through and retry
				}
			}
		} finally {
			if (interrupted)
				Thread.currentThread().interrupt();
		}
	}

}

class Foo {
	void interrupt() {};
}
