import java.util.Random;


/**
 * A Deck is a specialized card stack that initially has all cards shuffled according to a given random number seed.
 * One cannot play to/from a Deck; it is only used for dealing into other stacks.
 */
public class Deck extends CardStack {
	private static Random random = new Random(0); 
	
	/**
	 * Create a shuffled deck of cards according to the given random number seed.
	 * @param seed random seed for deck shuffle
	 */
	public Deck(long seed) {
		for (Card c : Card.getAllCards())
			addCard(c);
		random.setSeed(seed);
		for (int i = stack.size() - 1; i >= 1; i--) {
			int j = random.nextInt(i + 1);
			Card c = stack.get(i);
			stack.setElementAt(stack.get(j), i);
			stack.setElementAt(c, j);
		}
	}

	/** 
	 * One cannot play from a Deck.
	 * @return false;
	 */
	@Override
	public boolean canPlayFrom() throws IllegalPlayException {
		throw new IllegalPlayException("That card stack cannot be played from.");
	}
	/** 
	 * One cannot play to a Deck.
	 * @return false
	 */
	public boolean playTo(Card card) throws java.lang.NullPointerException, IllegalPlayException {
		if(card == null) {
			throw new java.lang.NullPointerException();
		}
		throw new IllegalPlayException("Cannot play to the deck.");
	}
}