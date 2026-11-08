public class CardStack{
	protected java.util.Stack<Card> stack;
	
	//Constructor
	public CardStack() {
		this.stack = new java.util.Stack<Card>();
	}

	//method
	public void addCard(Card card) {
		stack.push(card);
	}

	public boolean canPlayFrom() throws IllegalPlayException {
		if(stack.isEmpty()) {
			throw new IllegalPlayException("You cannot play from an empty stack.");
		}
		return true;
	}

	public Card getTopCard() throws java.util.EmptyStackException {
		if(stack.isEmpty()) {
			throw new java.util.EmptyStackException();
		}
		return stack.peek();
	}

	public boolean isEmpty() {
		return stack.isEmpty();
	}
	
	public boolean playTo(Card card) throws IllegalPlayException, java.lang.NullPointerException{
		if(card == null) {
			throw new java.lang.NullPointerException();
		}
		stack.add(card);
		return true;
	}
	
	public boolean playTo(CardStack otherStack) throws IllegalPlayException {
		if (otherStack == null) {
			throw new NullPointerException();
		}

		if (!otherStack.canPlayFrom()) {
			return false;
		}

		Card card = otherStack.getTopCard();
		if (!playTo(card)) {
			return false;
		}

		otherStack.removeTopCard();
		return true;
	}
	
	public Card removeTopCard() {
		if (stack.isEmpty()) {
			return null;
		}
		return stack.pop();
	}

	public int size() {
		return stack.size();
	}

	public Card[] toArray() {
		Card[] cards = new Card[stack.size()];

		for(int i = 0; i < stack.size(); i++) {
			cards[i] = stack.get(i);
		}
		return cards;
	}
	
	public String toString() {
		String result = "";
		for (Card card : toArray()) {
			if (!result.isEmpty()) {
				result += ", ";
			}
			result += card;
		}
		return result;
	}
}