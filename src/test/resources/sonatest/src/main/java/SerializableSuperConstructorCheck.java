import java.io.Serializable;
public class SerializableSuperConstructorCheck extends Fruit implements Serializable {  // Noncompliant
    private String variety;

    public SerializableSuperConstructorCheck(String ripe, String variety) {
    }

    public void setVariety(String variety) {
    }

    public String getVarity() {
    }
}

public class Fruit {
    private String ripe;

    public Fruit (String ripe) {}
    public void setRipe(String ripe) {}
    public String getRipe() {}
}