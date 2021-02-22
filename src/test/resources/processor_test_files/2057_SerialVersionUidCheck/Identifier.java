import java.io.Serializable;

// Test for rule s2057
public class WrongIdentifier implements Serializable { // Noncompliant

  long serialVersionUID = 1L;
  
}
