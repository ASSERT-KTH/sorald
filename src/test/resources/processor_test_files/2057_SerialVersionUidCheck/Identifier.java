import java.io.Serializable;

// Test for rule s2057
public class Identifier implements Serializable { // Noncompliant

  long serialVersionUID = 1L;
  
}
