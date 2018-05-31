package spoon.reflect.factory;


public abstract class SubFactory {
    protected Factory factory;

    public SubFactory(Factory factory) {
        super();
        this.factory = factory;
    }

    /* [Spoon inserted constructor], repairs sonarqube rule 2055:
    The non-serializable super class of a "Serializable" class should have a no-argument constructor
    This class is a superclass of ModuleFactory
     */
    public SubFactory() {
    }
}

