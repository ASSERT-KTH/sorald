public  class CatchBlockWithBreak {
    public static void main() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException exception) {
                break;
            }
        }
    }
}
