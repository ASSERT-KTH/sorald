package sorald;

public class ThreadRunInvocation {
    public void method(){
        Thread myThread = new Thread();
        myThread.run(); // Noncompliant
    }
}
