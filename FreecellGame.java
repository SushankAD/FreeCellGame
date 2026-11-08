import java.util.ArrayDeque;
import java.util.Deque;

public class FreecellGame {
	private final CardStack[] stacks;
	private final Deck deck;
	private final Deque<Move> moveHistory = new ArrayDeque<>();

	public FreecellGame(long seed) {
		this(seed, false);
	}

	private FreecellGame(long seed, boolean oneMoveFromWin) {
		this.stacks = new CardStack[17];

		for(int stackNum = 1; stackNum <= 4; stackNum++){
			stacks[stackNum] = new Cell();
		}

		for(int stackNum = 5; stackNum <= 8; stackNum++) {
			stacks[stackNum] = new Foundation(stackNum - 5);
		}

		for(int stackNum = 9; stackNum <= 16; stackNum++) {
			stacks[stackNum] = new Cascade();
		}
		
		deck = new Deck(seed);
		stacks[0] = deck;
		if (oneMoveFromWin) {
			createOneMoveFromWinDeal();
			return;
		}

		int stackNum = 9;
		
		for(int i = 0; i < Card.getDeckSize(); i++) {
			Card topCard = deck.removeTopCard();
			stacks[stackNum].addCard(topCard);
			stackNum++;
			
			if(stackNum >16) {
				stackNum = 9;
			}
		}
	}

	public static FreecellGame oneMoveFromWin() {
		return new FreecellGame(0, true);
	}

	private void createOneMoveFromWinDeal() {
		while (deck.removeTopCard() != null) {
			// The demo deal uses a known arrangement instead of the shuffled deck.
		}
		for (int suit = 0; suit < 4; suit++) {
			for (int rank = 0; rank < 13; rank++) {
				if (suit == 0 && rank == 12) {
					continue;
				}
				stacks[5 + suit].addCard(new Card(rank, suit));
			}
		}
		stacks[9].addCard(new Card(12, 0));
	}

	public Card[] getStack(int stackNum) {
		return stacks[stackNum].toArray();
	}

	public boolean play(int srcStackNum, int destStackNum) throws IllegalPlayException {
		if (srcStackNum < 1 || srcStackNum > 16
				|| destStackNum < 1 || destStackNum > 16) {
			throw new IllegalPlayException("Illegal stack number. Stacks are numbered 1-16.");
		}

		if (srcStackNum == destStackNum) {
			throw new IllegalPlayException("Source and destination stacks must be different.");
		}
		boolean moved = stacks[destStackNum].playTo(stacks[srcStackNum]);
		if (moved) {
			moveHistory.push(new Move(srcStackNum, destStackNum));
		}
		return moved;
	}

	public boolean canUndo() {
		return !moveHistory.isEmpty();
	}

	public boolean undo() {
		if (!canUndo()) {
			return false;
		}
		Move move = moveHistory.pop();
		Card card = stacks[move.destinationStack].removeTopCard();
		if (card == null) {
			throw new IllegalStateException("Cannot undo move: destination stack is empty.");
		}
		stacks[move.sourceStack].addCard(card);
		return true;
	}
	
	public boolean isGameOver() {
		int foundationCount = 0;
		
		for(int stackNum = 5; stackNum <= 8; stackNum++) {
			foundationCount += stacks[stackNum].size();
		}
		return foundationCount == Card.getDeckSize();
	}
	
	public String toString() {
		String result1 = "Cells:";
		for(int stackNum = 1; stackNum <= 4; stackNum++) {
			result1 += String.format("\n%2d: %s", stackNum, formatStack(stackNum));
		}
		
		String result2 = "\nFoundations:";
		for(int stackNum = 5; stackNum <= 8; stackNum++) {
			result2 += String.format("\n%2d: %s", stackNum, formatStack(stackNum));
		}
		
		String result3 = "\nCascades:";
		for(int stackNum = 9; stackNum <= 16; stackNum++) {
			result3 += String.format("\n%2d: %s", stackNum, formatStack(stackNum));
		}	
		return result1 + result2 + result3;
	}

	private String formatStack(int stackNum) {
		String result = "";
		for(Card card : stacks[stackNum].toArray()) {
			if(!result.isEmpty()) {
				result += ", ";
			}
			result += card;
		}
		return result;
	}

	private static final class Move {
		private final int sourceStack;
		private final int destinationStack;

		private Move(int sourceStack, int destinationStack) {
			this.sourceStack = sourceStack;
			this.destinationStack = destinationStack;
		}
	}
}
