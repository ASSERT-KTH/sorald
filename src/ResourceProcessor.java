import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtExecutableReference;

/**
 * Adds a not-null check for all method parameters which are objects
 *
 * @author Martin Monperrus
 */
public class ResourceProcessor extends AbstractProcessor<CtConstructorCall> {
	@Override
	public void process(CtConstructorCall element) {
		System.out.println(element+"\nHello\n");
		// we declare a new snippet of code to be inserted.
        /*
		CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();

		// this snippet contains an if check.
		final String value = String.format("if (%s == null) "
				+ "throw new IllegalArgumentException(\"[Spoon inserted check] null passed as parameter\");",
				element.getSimpleName());
		snippet.setValue(value);

		// we insert the snippet at the beginning of the method body.
		if (element.getParent(CtExecutable.class).getBody() != null) {
			element.getParent(CtExecutable.class).getBody().insertBegin(snippet);
		}
		*/
	}
}