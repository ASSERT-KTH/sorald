public class ViolationAndNonViolationOnSameLine
{
    public void dead()
    {
        int x = 5;
        int y = 2;
        System.out.println(x + y);
        x = 2; y = 3; // Noncompliant
        System.out.println(y);
    }
}
