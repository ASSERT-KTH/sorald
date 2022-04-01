package sorald.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
/** Annotation to mark that a processor is only a partial fix for its associated rule. */
public @interface IncompleteProcessor {
    /** @return A description as to why and how the processor is incomplete. */
    String description();
}
