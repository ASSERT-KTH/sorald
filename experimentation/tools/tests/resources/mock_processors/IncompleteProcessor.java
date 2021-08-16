@IncompleteProcessor(description = "does not fix variable naming")
@ProcessorAnnotation(key = "S1444", description = "\"public static\" fields should be constant")
public class PublicStaticFieldShouldBeFinalProcessor extends SoraldAbstractProcessor<CtField<?>> {
    @Override
    protected void repairInternal(CtField<?> element) {
        element.addModifier(ModifierKind.FINAL);
    }
}
