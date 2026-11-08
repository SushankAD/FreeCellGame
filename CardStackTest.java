public class CardStackTest {
	public static void main(String[] args) {
		CardStack stack = new CardStack();

		try {
			stack.getTopCard();
			System.out.println("fail: getTopCard did not throw");
		} catch (java.util.EmptyStackException e) {
			System.out.println("pass: getTopCard threw EmptyStackException");
		}

		if (stack.removeTopCard() == null) {
			System.out.println("pass: removeTopCard returns null for an empty stack");
		} else {
			System.out.println("fail: removeTopCard should return null for an empty stack");
		}

		Card card = new Card(0, 0);
		stack.addCard(card);

		System.out.println("top card: " + stack.getTopCard());
		System.out.println("removed card: " + stack.removeTopCard());
		System.out.println("stack empty: " + stack.isEmpty());
	}
}
