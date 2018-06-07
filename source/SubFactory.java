package spoon.reflect.factory;


public abstract class SubFactory {
    protected spoon.reflect.factory.Factory factory;

    public SubFactory(spoon.reflect.factory.Factory factory) {
        super();
        this.factory = factory;
    }
}

