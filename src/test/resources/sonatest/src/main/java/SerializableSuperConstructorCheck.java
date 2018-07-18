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