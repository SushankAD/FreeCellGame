public class Cell extends CardStack{

	//constructor
	public Cell() {
		super();
	}

	//methods
	public boolean playTo(Card card) throws NullPointerException, IllegalPlayException {
		if (card == null) {
			throw new NullPointerException();
		}
		if (!stack.isEmpty()) {
			throw new IllegalPlayException("Cells may only contain a single card.");
		}
		stack.add(card);
		return true;
	}
	
}
