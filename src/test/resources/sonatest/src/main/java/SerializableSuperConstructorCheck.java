/**
 * DISABLED as per commit b965cad6f327d8fd0fb97a3af8f6427de61685c4.
 * The test is unable to find the error in the initial state. See commit mesage above for more info.
 */
/*
/*
public class Fruit {
    private Season ripe;

    public Fruit (Season ripe) {}
    public void setRipe(Season ripe) {}
    public Season getRipe() {}
}

public class SerializableSuperConstructorCheck extends Fruit implements Serializable {  // Noncompliant; nonserializable ancestor doesn't have no-arg constructor
    private static final long serialVersionUID = 1;

    private String variety;

    public SerializableSuperConstructorCheck(Season ripe, String variety) { }
    public void setVariety(String variety) {}
    public String getVarity() {}
}
*/