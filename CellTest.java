
public class CellTest {
	public static void main(String[] args) throws IllegalPlayException {
		Cell cell = new Cell();

		try {
			Card card = null;
			cell.playTo(card);
			System.out.println("fail: null card was accepted");
		} catch (NullPointerException e) {
			System.out.println("pass: null card was rejected");
		}

		try {
			cell.addCard(new Card(6, 2));
			cell.playTo(new Card(10, 1));
			System.out.println("fail: illegal play was accepted");
		} catch (IllegalPlayException e) {
			System.out.println("pass: illegal play was rejected");
		}
	}
}
