import org.json.JSONArray;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Adds a not-null check for all method parameters which are objects
 *
 * @author Martin Monperrus
 */
public class NotNullCheckAdderProcessor extends AbstractProcessor<CtExecutableReference<?>> {

    /*
        @Override
        public boolean isToBeProcessed(CtExecutableReference<?> element) {
            boolean answer = !element.getType().isPrimitive();// only for objects
            System.out.println(element +" is to be processed? "+ (answer?" Yes":" No"));
            return true;
        }
        */
	private static boolean canReturnNull(CtExecutable<?> exec)
    {
        CtBlock block=exec.getBody();
        List<CtReturn> elements = block.getElements(new TypeFilter<>(CtReturn.class));
        for(CtReturn xx:elements)
        {
            CtExpression retexpr = xx.getReturnedExpression();
            String strret=retexpr.toString();
            if(strret.equals("null"))
            {
                return true;
            }
            System.out.println(retexpr.getType()+ " type");
            System.out.println("RETURN "+retexpr+" \nXXXXXXXXXXXXXXXXXXXXXXXX\n");
        }
        return true;
    }
	@Override
	public void process(CtExecutableReference<?> element) {
		// we declare a new snippet of code to be inserted.
		CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();

		// this snippet contains an if check.
		final String value = String.format("if (%s == null) "
						+ "throw new IllegalArgumentException(\"[Spoon inserted check] null passed as parameter\");",
				element.getSimpleName());
		final String temp = "HRYRHRDSSSSSSSSSSSSSSSSFSDFSDFSDF \n\n" + element.getSimpleName() + "\n\nEND\n";
		snippet.setValue(temp);

		// we insert the snippet at the beginning of the method body.
//			if (element.getParent(CtExecutable.class).getBody() != null) {
		//Method ctm = element.getActualMethod();
        CtExecutable cte=element.getDeclaration();
		if(cte!=null)
		{

            String name= cte.getSimpleName();
            if(!name.equals("getValue"))
            {
//                System.out.println("not equal");

            }
            else
            {

                System.out.println(name+" hello");
                boolean retnull=canReturnNull(cte);
                if(retnull)
                {
                    System.out.println(element+" can return null");
                }

                System.out.println("\nEEEEEEEEEEEEEEEEEEEEEEEEE\n\n");
            }
		}
		else
		{
//			System.out.println();
		}
//		System.out.println();
		/*
		if (ctm != null) {
			System.out.println(ctm.toString() + "\nhello\n");
		}
		else
		{
			System.out.println(ctm+" Is null \n");
		}
		*/
		//final String x=ctm.toString();
//		System.out.println(x +"\nhello\n");
		//CtElement =element.getParent();
//		System.out.println(element+"\n Hello\n");
		if(true)
        {
			//	element.getParent(CtExecutable.class).getBody().insertBegin(snippet);
//			CtComment com= getFactory().createComment("hello",CtComment.CommentType.BLOCK);
//			System.out.println(com+"        sdfsdfsdfffffff");lahore greetinglahore greetinglahore greeting
//			element.addComment(com);
//			CtParameter elem= getFactory().createParameter(getFactory().createConstructor(), getFactory().createTypeReference(), temp);
//			element.replace(elem);
		}
	}
}