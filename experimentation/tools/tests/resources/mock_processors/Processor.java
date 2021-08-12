package sorald.processor;

import sorald.annotations.ProcessorAnnotation;
import spoon.reflect.declaration.CtField;

@ProcessorAnnotation(key = "S1068", description = "Unused \"private\" fields should be removed")
public class UnusedPrivateFieldProcessor extends SoraldAbstractProcessor<CtField<?>> { }
