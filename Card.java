/**
 * A class for representing standard (French) playing cards.
 * Rank numbers are 0 through 12, corresponding to possible rank names: "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K".
 * Suit numbers are 0 through 3, corresponding to possible suit names: "C", "d", "h", "S".
 * Note that upper/lowercase suit names are black/red suits.
 * The toString method for each card will be the concatenation of a rank name with a suit name.
 */
public class Card {

	/**
	 * an array of all unique Card objects
	 */
	private static final Card[] ALL_CARDS;
	
	/**
	 * array of abbreviated card rank names in ascending order of rank
	 */
	private static final String[] RANK_NAMES = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};

	/**
	 * array of abbreviated card suit names with lowercase red suits
	 */
	private static final String[] SUIT_NAMES = {"C", "d", "h", "S"};

	/**
	 * parallel array to suitNames indicating whether or not the corresponding suit is red
	 */
	private static final boolean[] SUIT_IS_RED = {false, true, true, false};

	static {
		ALL_CARDS = new Card[RANK_NAMES.length * SUIT_NAMES.length];
		int i = 0;
		for (int suit = 0; suit < SUIT_NAMES.length; suit++)
			for (int rank = 0; rank < RANK_NAMES.length; rank++)
				ALL_CARDS[i++] = new Card(rank, suit);
	}

	private final int rank;
	private final int suit;

	/**
	 * Constructor to create a card object with the corresponding zero-based indices to rankNames and suitNames, respectively
	 * @param rank rank of card (zero-based index to rankNames)
	 * @param suit suit of card (zero-based index to suitNames)
	 */
	public Card(int rank, int suit) {
		if (rank < 0 || rank >= RANK_NAMES.length) {
			throw new IllegalArgumentException("Rank must be between 0 and 12.");
		}
		if (suit < 0 || suit >= SUIT_NAMES.length) {
			throw new IllegalArgumentException("Suit must be between 0 and 3.");
		}
		this.rank = rank;
		this.suit = suit;
	}

	/**
	 * Get a copy of the standard 52-card deck.
	 * @return a copy of the standard cards
	 */
	public static Card[] getAllCards() {
		return ALL_CARDS.clone();
	}

	public static int getDeckSize() {
		return ALL_CARDS.length;
	}

	public static String getRankName(int rank) {
		return RANK_NAMES[rank];
	}

	public static String getSuitName(int suit) {
		return SUIT_NAMES[suit];
	}

	public static boolean isRedSuit(int suit) {
		return SUIT_IS_RED[suit];
	}

	/**
	 * Get rank of card (zero-based index to rankNames).
	 * @return rank of card (zero-based index to rankNames)
	 */
	public int getRank() {
		return rank;
	}

	/**
	 * Get suit of card (zero-based index to suitNames).
	 * @return suit of card (zero-based index to suitNames)
	 */
	public int getSuit() {
		return suit;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public java.lang.String toString() {
		return RANK_NAMES[rank] + SUIT_NAMES[suit];
	}
}
