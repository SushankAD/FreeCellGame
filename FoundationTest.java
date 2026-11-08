
public class FoundationTest {
	public static void main(String[] args) throws IllegalPlayException {
		Foundation foundation = new Foundation();

		try {
			foundation.playTo(new Card(1, 0));
			System.out.println("fail: non-ace was accepted as the first foundation card");
		} catch (IllegalPlayException e) {
			System.out.println("pass: " + e.getMessage());
		}

		foundation.playTo(new Card(0, 0));

		try {
			foundation.playTo(new Card(1, 1));
			System.out.println("fail: card with a different suit was accepted");
		} catch (IllegalPlayException e) {
			System.out.println("pass: " + e.getMessage());
		}

		try {
			foundation.playTo(new Card(2, 0));
			System.out.println("fail: card with the wrong rank was accepted");
		} catch (IllegalPlayException e) {
			System.out.println("pass: " + e.getMessage());
		}

		try {
			foundation.canPlayFrom();
			System.out.println("fail: playing from a foundation was allowed");
		} catch (IllegalPlayException e) {
			System.out.println("pass: " + e.getMessage());
		}

		try {
			foundation.playTo((Card) null);
			System.out.println("fail: null card was accepted");
		} catch (NullPointerException e) {
			System.out.println("pass: null card was rejected");
		}
	}
}
