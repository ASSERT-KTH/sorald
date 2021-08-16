@ProcessorAnnotation(
        description = "\"Iterator.next()\" methods should throw \"NoSuchElementException\"",
        key = "S2272")
public class IteratorNextExceptionProcessor extends SoraldAbstractProcessor<CtMethod> { }
