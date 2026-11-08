public class CardTest {
	public static void main(String[] args) {
		Card aceOfClubs = new Card(0, 0);

		System.out.println("Testing constructor:");
		System.out.println("Card: " + aceOfClubs);

		System.out.println();
		System.out.println("Testing getRank:");
		System.out.println("Rank: " + aceOfClubs.getRank());

		System.out.println();
		System.out.println("Testing getSuit:");
		System.out.println("Suit: " + aceOfClubs.getSuit());

		System.out.println();
		System.out.println("Testing toString:");
		System.out.println(aceOfClubs.toString());

		System.out.println();
		System.out.println("Testing allCards:");
		System.out.println("Cards in a deck: " + Card.getDeckSize());
	}
}
