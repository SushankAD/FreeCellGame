public class Cascade extends CardStack{

	//constructor
	public Cascade() {
		super();
	}

	//method
	@Override
	public boolean playTo(Card card) throws NullPointerException, IllegalPlayException {
		if (card == null) {
			throw new NullPointerException();
		}

		if(stack.isEmpty()) {
			stack.add(card);
			return true;
		}

		Card topCard = stack.peek();

		boolean topIsRed = Card.isRedSuit(topCard.getSuit());
		boolean cardIsRed = Card.isRedSuit(card.getSuit());
		boolean isDifferentColor = (topIsRed != cardIsRed);
		boolean isNextLowerRank = (topCard.getRank() - card.getRank() == 1);

		if (!isDifferentColor) {
			throw new IllegalPlayException("Plays to a cascade must alternate in suit color.");
		}
		if (!isNextLowerRank) {
			throw new IllegalPlayException("Plays to a cascade must have the next decreasing rank.");
		}
		stack.add(card);
		return true;
	}
}
