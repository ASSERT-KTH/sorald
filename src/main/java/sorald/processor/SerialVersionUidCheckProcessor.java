package sorald.processor;

import java.io.Serializable;
import java.util.Set;
import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
@ProcessorAnnotation(
  key =  2057,
  description = "Every class implementing Serializable should declare a private static final serialVersionUID. Subclasses should have there own uid"
)
public class SerialVersionUidCheckProcessor extends SoraldAbstractProcessor<CtClass<?>> {


  private static final String SERIAL_VERSION_UID = "serialVersionUID";
  private Set<ModifierKind> modifiers = Set.of(ModifierKind.PRIVATE, ModifierKind.STATIC, ModifierKind.FINAL);
  @Override
  protected boolean canRepairInternal(CtClass<?> candidate) {
    Set<CtTypeReference<?>> superInterfaces = candidate.getSuperInterfaces();
    CtTypeReference<?> serializable = candidate.getFactory().createCtTypeReference(Serializable.class);
    // check if the class implements serializable
    if(!superInterfaces.contains(serializable)) {
      // no serializable found => nothing to repair
      return false;
    }
    CtFieldReference<?> fieldReference = getSerialVersionUIDField(candidate);
    // class implements serializable, lets check for serialVersionUID field 
    if(fieldReference != null) {
      CtField<?> field = fieldReference.getDeclaration();
     // check all modifiers, the field should be private static final
     return !(field.isFinal() && field.isPrivate() && field.isStatic());
    }
    // here should the class implement serializable and doesn't have a serialVersionUID with proper type
    return true;
  }

  private CtFieldReference<?> getSerialVersionUIDField(CtClass<?> candidate) {
    return candidate.getAllFields().stream()
        .filter(v -> v.getSimpleName().equals(SERIAL_VERSION_UID) && v.getType().equals(getLongPrimitiveType(candidate)))
        .findFirst().orElse(null);
  }
  private CtTypeReference<Long> getLongPrimitiveType(CtElement element) {
    return element.getFactory().Type().longPrimitiveType();
  }
  @Override
  protected void repairInternal(CtClass<?> element) {
    CtFieldReference<?> serialVersionUidReference = getSerialVersionUIDField(element);
    if (serialVersionUidReference == null) {
      // serialVersionUID Field missing => add the field
      CtField<?> replacement = element.getFactory().createField(null, modifiers,getLongPrimitiveType(element),SERIAL_VERSION_UID);
      replacement.setDefaultExpression(element.getFactory().createCodeSnippetExpression("1L"));
      element.addFieldAtTop(replacement);
    }
    else {
      // in this case the field exists but the modifiers are wrong.
      element.setModifiers(modifiers);
    }
  }
  
}
