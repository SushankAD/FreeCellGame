public class Foundation extends CardStack {
	private final int suit;

	public Foundation() {
		this(-1);
	}

	public Foundation(int suit) {
		super();
		if (suit < -1 || suit > 3) {
			throw new IllegalArgumentException("Suit must be between 0 and 3.");
		}
		this.suit = suit;
	}
	
	@Override
	public boolean canPlayFrom() throws IllegalPlayException {
		throw new IllegalPlayException("That card stack cannot be played from.");
	}
	
	@Override
	public boolean playTo(Card card) throws NullPointerException, IllegalPlayException {
		if (card == null) {
			throw new NullPointerException();
		}

		if (stack.isEmpty()) {
			if (card.getRank() != 0) {
				throw new IllegalPlayException("The first foundation card must be an Ace.");
			}
			if (suit >= 0 && card.getSuit() != suit) {
				throw new IllegalPlayException("The first foundation card must match its suit.");
			}
			stack.push(card);
			return true;
		}

		Card topCard = stack.peek();
		if (card.getSuit() != topCard.getSuit()) {
			throw new IllegalPlayException("Plays to a foundation must match suit.");
		}
		if (card.getRank() != topCard.getRank() + 1) {
			throw new IllegalPlayException("Plays to a foundation must have the next increasing rank.");
		}
		stack.add(card);
		return true;
	}
}
