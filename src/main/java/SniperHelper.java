import org.json.JSONArray;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;

import java.util.List;

public class SniperHelper
{
    public static void process(CtClass element, JSONArray array)throws Exception
    {
        NullDereferenceProcessor ndp = new NullDereferenceProcessor(array);
        List<CtInvocation> invo = element.getElements(e -> e instanceof CtInvocation);
        invo.removeIf(ctInvocation -> !ndp.isToBeProcessed(ctInvocation));
        for(CtInvocation i:invo)
        {
            System.out.println(i);
        }
    }
}
