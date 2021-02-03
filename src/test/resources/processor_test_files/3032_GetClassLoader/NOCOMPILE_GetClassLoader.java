import javax.ejb.embeddable.EJBContainer;

public class NOCOMPILE_GetClassLoader {

	ClassLoader d = this.getClass().getClassLoader(); // Noncompliant

	public void case1() {
		ClassLoader c = this.getClass().getClassLoader(); // Noncompliant
	}

	public void case2() throws ClassNotFoundException{
		Dummy.class.getClassLoader().loadClass("anotherclass"); // Noncompliant
	}

	abstract class InnerClass {
		ClassLoader f = this.getClass().getClassLoader(); // Noncompliant

		public void hello() {
			this.getClass().getClassLoader(); // Noncompliant
		}
	}

	public void usingEJB() {
		EJBContainer container = new EJBContainer();
	}
}

class Dummy {}
