public class GenericException {
    public void run() {
        try {
            throw new InterruptedException();
        } catch (InterruptedException | Exception e) {

        }
    }
}
