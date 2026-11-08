
public class DeckTest {
	public static void main(String[] args) {
		Deck deck = new Deck(0);

		try {
			deck.canPlayFrom();
			System.out.println("fail: playing from a deck was allowed");
		} catch (IllegalPlayException e) {
			System.out.println("pass: " + e.getMessage());
		}

		try {
			deck.playTo(new Card(0, 0));
			System.out.println("fail: playing to a deck was allowed");
		} catch (IllegalPlayException e) {
			System.out.println("pass: " + e.getMessage());
		}
	}
}
