

public class DeadStores {
	public void dead() {
		int x = 5;
		int y = 10;
		y = (x * x) * y;
		System.out.println(y);
		//[Spoon inserted check], repairs sonarqube rule 1854:Dead stores should be removed,
		//useless assignment to x removed;
	}
}

