public class DeadStores
{
    public void dead()
    {
        int x=5;
        int y=10;
        int z;
        y=x*x*y;
        System.out.println(y);
        x=5;// Noncompliant
        y=10;// Noncompliant
        z=20;// Noncompliant
    }
}
