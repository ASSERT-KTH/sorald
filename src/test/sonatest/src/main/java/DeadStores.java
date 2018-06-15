public class DeadStores
{
    public void dead()
    {
        int x=5;
        int y=10;
        y=x*x*y;
        System.out.println(y);
        x=5;
        y=10;
    }
}
