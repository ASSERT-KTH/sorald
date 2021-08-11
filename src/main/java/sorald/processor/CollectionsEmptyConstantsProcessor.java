package sorald.processor;

import java.util.Collections;
import org.apache.commons.lang.StringUtils;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;

// @spotless:off
/**
 * The `EMPTY_...` fields from `Collections` return raw types, so they are replaced by the `empty...()` methods that return generic ones.
 *
 * Example:
 * ```diff
 * - List<String> collection1 = Collections.EMPTY_LIST;  // Noncompliant
 * - Map<String, String> collection2 = Collections.EMPTY_MAP;  // Noncompliant
 * - Set<String> collection3 = Collections.EMPTY_SET;  // Noncompliant
 * + List<String> collection1 = Collections.emptyList();
 * + Map<String, String> collection2 = Collections.emptyMap();
 * + Set<String> collection3 = Collections.emptySet();
 * ```
 */
// @spotless:on
@ProcessorAnnotation(
        key = "S1596",
        description =
                "\"Collections.EMPTY_LIST\", \"EMPTY_MAP\", and \"EMPTY_SET\" should not be used")
public class CollectionsEmptyConstantsProcessor extends SoraldAbstractProcessor<CtFieldAccess<?>> {

    @Override
    protected void repairInternal(CtFieldAccess<?> element) {
        String[] loweredNameParts = element.getVariable().getSimpleName().toLowerCase().split("_");
        String camelCasedName = loweredNameParts[0] + StringUtils.capitalize(loweredNameParts[1]);
        CtMethod<?> methodToBeCalled =
                getFactory().Class().get(Collections.class).getMethod(camelCasedName);

        CtInvocation<?> newInvocation =
                getFactory().createInvocation(element.getTarget(), methodToBeCalled.getReference());

        element.replace(newInvocation);
    }
}
