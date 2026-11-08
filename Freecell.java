import java.util.Random;
import java.util.Scanner;
import java.util.InputMismatchException;

public class Freecell {

	private static final Scanner scanner = new Scanner(System.in);
	private FreecellGame game;

	public Freecell() {
	}

	public void play() {
		Random random = new Random();
		long seed = random.nextLong();
		play(seed);
	}

	public void play(long seed) {
		game = new FreecellGame(seed);
		while(!game.isGameOver()) {
			System.out.println(game);
			System.out.print("Enter source and destination stack numbers (1-16): ");

			if (!scanner.hasNext()) {
				return;
			}
			int source;
			int destination;
			try {
				source = scanner.nextInt();
				if (!scanner.hasNext()) {
					return;
				}
				destination = scanner.nextInt();
			} catch (InputMismatchException e) {
				scanner.next();
				System.out.println("Source and destination card stacks must be entered as integers (1-16).");
				continue;
			}

			try {
				if (!game.play(source, destination)) {
					System.out.println("That move is not legal.");
				}
			} catch (IllegalPlayException e) {
				System.out.println("Illegal play: " + e.getMessage());
			}
		}
		System.out.println(game);
		System.out.println("You won the game!");
	}

	public static void main(String[] args) {
		FreecellWindow.main(args);
	}
}
