
public class FreecellGameTest {
	public static void main(String[] args) throws IllegalPlayException {
		FreecellGame game = new FreecellGame(0);

		try {
			game.play(1, 1);
			System.out.println("fail: same source and destination was accepted");
			System.exit(1);
		} catch (IllegalPlayException e) {
			System.out.println("pass: same source and destination was rejected");
		}

		try {
			game.play(1, 20);
			System.out.println("fail: out-of-bounds stack was accepted");
			System.exit(1);
		} catch (IllegalPlayException e) {
			System.out.println("pass: out-of-bounds stack was rejected");
		}
	}
}
