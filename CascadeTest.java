
public class CascadeTest {
	public static void main(String[] args) throws IllegalPlayException {
		Cascade cascade = new Cascade();

		cascade.playTo(new Card(6, 0));
		cascade.playTo(new Card(5, 1));
		System.out.println("valid moves succeeded");

		try {
			cascade.playTo(new Card(6, 2));
			System.out.println("fail: illegal move was accepted");
		} catch (IllegalPlayException e) {
			System.out.println("pass: " + e.getMessage().toLowerCase());
		}

		try {
			Card card = null;
			cascade.playTo(card);
			System.out.println("fail: null card was accepted");
		} catch (NullPointerException e) {
			System.out.println("pass: null card was rejected");
		}
	}
}
