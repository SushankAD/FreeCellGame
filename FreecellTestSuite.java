
import java.util.HashSet;
import java.util.Set;
import java.awt.image.BufferedImage;

public class FreecellTestSuite {
	public static void main(String[] args) throws Exception {
		testCard();
		testCardArt();
		testDeck();
		testCascade();
		testCell();
		testFoundation();
		testGame();
		testOneMoveFromWinDeal();
		testGameMoveEdgeCases();
		testIllegalGameMoveIsTransactional();
		testGameAcrossDeals();
		testElapsedTimeFormatting();
		testWinRateFormatting();
		System.out.println("All FreeCell tests passed.");
	}

	private static void testElapsedTimeFormatting() {
		assert "00:00".equals(FreecellWindow.formatElapsedTime(0)) : "timer formats zero";
		assert "00:59".equals(FreecellWindow.formatElapsedTime(59)) : "timer formats seconds";
		assert "01:00".equals(FreecellWindow.formatElapsedTime(60)) : "timer rolls over to minutes";
		assert "02:05".equals(FreecellWindow.formatElapsedTime(125))
				: "timer formats minutes and seconds";
		assert "100:00".equals(FreecellWindow.formatElapsedTime(6000))
				: "timer supports sessions longer than 99 minutes";
	}

	private static void testWinRateFormatting() {
		assert "0%".equals(FreecellWindow.formatWinRate(0, 0))
				: "win rate displays percent with no concluded deals";
		assert "0%".equals(FreecellWindow.formatWinRate(0, 1)) : "zero wins display percent";
		assert "33%".equals(FreecellWindow.formatWinRate(1, 3))
				: "win rate rounds to a whole percent";
		assert "100%".equals(FreecellWindow.formatWinRate(2, 2))
				: "all wins display full percentage";
	}

	private static void testCardArt() {
		for (int rank = 0; rank < 13; rank++) {
			for (int suit = 0; suit < 4; suit++) {
				BufferedImage image = CardArt.get(rank, suit);
				assert image.getWidth() >= 200 : "card image has useful width";
				assert image.getHeight() >= 300 : "card image has useful height";
			}
		}
	}

	private static void testCard() {
		Card aceOfClubs = new Card(0, 0);
		assert "AC".equals(aceOfClubs.toString()) : "card string";
		assert aceOfClubs.getRank() == 0 : "card rank";
		assert aceOfClubs.getSuit() == 0 : "card suit";
		assertThrows(IllegalArgumentException.class, () -> new Card(-1, 0), "negative rank");
		assertThrows(IllegalArgumentException.class, () -> new Card(13, 0), "rank above king");
		assertThrows(IllegalArgumentException.class, () -> new Card(0, -1), "negative suit");
		assertThrows(IllegalArgumentException.class, () -> new Card(0, 4), "suit above range");

		Card[] exposedCards = Card.getAllCards();
		exposedCards[0] = new Card(1, 1);
		assert "AC".equals(Card.getAllCards()[0].toString()) : "deck copy is isolated";
		assert Card.getDeckSize() == 52 : "standard deck size";
	}

	private static void testDeck() {
		Card[] firstDeal = new Deck(42).toArray();
		Card[] secondDeal = new Deck(42).toArray();
		assert firstDeal.length == 52 : "deck contains 52 cards";
		Set<String> uniqueCards = new HashSet<>();
		for (int index = 0; index < firstDeal.length; index++) {
			uniqueCards.add(firstDeal[index].toString());
			assert firstDeal[index].toString().equals(secondDeal[index].toString())
					: "same seed produces same deal";
		}
		assert uniqueCards.size() == 52 : "deck contains each card once";
	}

	private static void testCascade() throws Exception {
		Cascade cascade = new Cascade();
		cascade.playTo(new Card(7, 1));
		cascade.playTo(new Card(6, 0));
		assert cascade.size() == 2 : "valid alternating descending build";
		assertThrows(IllegalPlayException.class, () -> cascade.playTo(new Card(5, 3)),
				"cascade rejects same-color card");
		assertThrows(IllegalPlayException.class, () -> cascade.playTo(new Card(4, 1)),
				"cascade rejects wrong rank");
		assert cascade.size() == 2 : "rejected cascade moves preserve cards";
	}

	private static void testCell() throws Exception {
		Cell cell = new Cell();
		cell.playTo(new Card(5, 2));
		assertThrows(IllegalPlayException.class, () -> cell.playTo(new Card(6, 0)),
				"cell holds one card");
		assert cell.size() == 1 : "full cell is unchanged";
	}

	private static void testFoundation() throws Exception {
		Foundation foundation = new Foundation();
		assertThrows(IllegalPlayException.class, () -> foundation.playTo(new Card(1, 0)),
				"foundation starts with ace");
		foundation.playTo(new Card(0, 0));
		foundation.playTo(new Card(1, 0));
		assertThrows(IllegalPlayException.class, () -> foundation.playTo(new Card(2, 1)),
				"foundation rejects different suit");
		assertThrows(IllegalPlayException.class, foundation::canPlayFrom,
				"foundation cannot be played from");
		assert foundation.size() == 2 : "rejected foundation moves preserve cards";

		Foundation clubsFoundation = new Foundation(0);
		assertThrows(IllegalPlayException.class, () -> clubsFoundation.playTo(new Card(0, 3)),
				"clubs foundation rejects a spade ace");
		assert clubsFoundation.isEmpty() : "rejected ace leaves its foundation empty";
		clubsFoundation.playTo(new Card(0, 0));
		assert clubsFoundation.size() == 1 : "clubs foundation accepts the club ace";
	}

	private static void testGame() throws Exception {
		FreecellGame game = new FreecellGame(1234);
		Set<String> dealtCards = new HashSet<>();
		for (int stackNumber = 1; stackNumber <= 16; stackNumber++) {
			Card[] cards = game.getStack(stackNumber);
			if (stackNumber >= 9) {
				assert cards.length == 6 || cards.length == 7
						: "initial cascade has six or seven cards";
			} else {
				assert cards.length == 0 : "cells and foundations start empty";
			}
			for (Card card : cards) {
				dealtCards.add(card.toString());
			}
		}
		assert dealtCards.size() == 52 : "initial deal contains all unique cards";
		assertThrows(IllegalPlayException.class, () -> game.play(1, 1),
				"game rejects same source and destination");
		assertThrows(IllegalPlayException.class, () -> game.play(1, 20),
				"game rejects invalid stack number");

		int[] legalMove = findLegalCascadeMove(game);
		int sourceSize = game.getStack(legalMove[0]).length;
		int destinationSize = game.getStack(legalMove[1]).length;
		assert game.play(legalMove[0], legalMove[1]) : "legal cascade move succeeds";
		assert game.getStack(legalMove[0]).length == sourceSize - 1
				: "successful move removes source card";
		assert game.getStack(legalMove[1]).length == destinationSize + 1
				: "successful move adds destination card";
		assert game.canUndo() : "successful move enables undo";
		assert game.undo() : "last move can be undone";
		assert game.getStack(legalMove[0]).length == sourceSize : "undo restores source stack";
		assert game.getStack(legalMove[1]).length == destinationSize : "undo restores destination stack";
	}

	private static void testGameMoveEdgeCases() throws Exception {
		FreecellGame game = new FreecellGame(77);
		String initialState = game.toString();
		assertThrows(IllegalPlayException.class, () -> game.play(1, 2),
				"empty source cannot be moved");

		int[] cascadeMove = findLegalCascadeMove(game);
		game.play(cascadeMove[0], cascadeMove[1]);
		assert game.canUndo() : "first legal move is undoable";
		int[] nextMove = findLegalMove(game);
		game.play(nextMove[0], nextMove[1]);
		assert game.undo() : "second move can be undone first";
		assert game.undo() : "first move can be undone second";
		assert initialState.equals(game.toString()) : "multiple undo restores full deal";
		FreecellGame cellGame = new FreecellGame(19);
		Card movedToCell = cellGame.getStack(9)[0];
		assert cellGame.play(9, 1) : "cascade card can move to an empty cell";
		assert cellGame.undo() : "move to cell can be undone";
		assert movedToCell.toString().equals(cellGame.getStack(9)[0].toString())
				: "undo restores card moved from cascade to cell";
		assert cellGame.getStack(1).length == 0 : "undo empties destination cell";

		FreecellGame foundationGame = null;
		int aceSource = -1;
		for (long seed = 0; seed < 1000 && aceSource < 0; seed++) {
			FreecellGame candidate = new FreecellGame(seed);
			int candidateSource = findAceStack(candidate);
			if (candidateSource >= 0) {
				foundationGame = candidate;
				aceSource = candidateSource;
			}
		}
		assert aceSource >= 0 : "test finds a deal with an accessible ace";
		int aceSuit = foundationGame.getStack(aceSource)[foundationGame.getStack(aceSource).length - 1]
				.getSuit();
		assert foundationGame.play(aceSource, 5 + aceSuit) : "ace can move to its foundation";
		assert foundationGame.undo() : "move to foundation can be undone";
		assert foundationGame.getStack(5 + aceSuit).length == 0 : "undo empties foundation";
		Card restoredAce = foundationGame.getStack(aceSource)
				[foundationGame.getStack(aceSource).length - 1];
		assert ("A" + Card.getSuitName(aceSuit)).equals(restoredAce.toString())
				: "undo restores ace to source";
	}

	private static void testOneMoveFromWinDeal() throws Exception {
		FreecellGame game = FreecellGame.oneMoveFromWin();
		int totalCards = 0;
		int foundationCards = 0;
		for (int stackNumber = 1; stackNumber <= 16; stackNumber++) {
			Card[] cards = game.getStack(stackNumber);
			totalCards += cards.length;
			if (stackNumber >= 5 && stackNumber <= 8) {
				foundationCards += cards.length;
			}
		}
		assert totalCards == 52 : "demo deal contains all cards";
		assert foundationCards == 51 : "demo deal has 51 cards in foundations";
		assert game.getStack(9).length == 1 : "demo deal leaves one card in a cascade";
		assert "KC".equals(game.getStack(9)[0].toString()) : "demo deal leaves the club king";
		assert !game.isGameOver() : "demo deal is not won before the final move";
		assert game.play(9, 5) : "club king moves to the club foundation";
		assert game.isGameOver() : "final demo move wins the game";
		assert game.undo() : "demo winning move can be undone";
		assert !game.isGameOver() : "undo returns the demo deal to one move from winning";
	}

	private static void testGameAcrossDeals() throws Exception {
		for (long seed = 0; seed < 100; seed++) {
			FreecellGame game = new FreecellGame(seed);
			Set<String> cards = new HashSet<>();
			int total = 0;
			for (int stackNumber = 1; stackNumber <= 16; stackNumber++) {
				Card[] stack = game.getStack(stackNumber);
				total += stack.length;
				for (Card card : stack) {
					cards.add(card.toString());
				}
			}
			assert total == 52 : "deal preserves all cards for seed " + seed;
			assert cards.size() == 52 : "deal has unique cards for seed " + seed;

			int[] move = findLegalMove(game);
			String initialState = game.toString();
			assert game.play(move[0], move[1]) : "seeded deal has an executable legal move";
			assert countCards(game) == 52 : "legal move preserves card count";
			assert game.undo() : "legal move in seeded deal can be undone";
			assert initialState.equals(game.toString()) : "undo restores seeded deal exactly";
		}
	}

	private static void testIllegalGameMoveIsTransactional() throws Exception {
		FreecellGame game = new FreecellGame(31415);
		int[] illegalMove = findIllegalCascadeMove(game);
		String initialState = game.toString();
		assertThrows(IllegalPlayException.class, () -> game.play(illegalMove[0], illegalMove[1]),
				"game rejects invalid cascade move");
		assert initialState.equals(game.toString())
				: "rejected game move leaves all stacks unchanged";
	}

	private static int[] findIllegalCascadeMove(FreecellGame game) {
		for (int source = 9; source <= 16; source++) {
			Card movingCard = game.getStack(source)[game.getStack(source).length - 1];
			for (int destination = 9; destination <= 16; destination++) {
				if (source == destination) {
					continue;
				}
				Card[] targetCards = game.getStack(destination);
				Card target = targetCards[targetCards.length - 1];
				if (Card.isRedSuit(movingCard.getSuit()) == Card.isRedSuit(target.getSuit())
						|| target.getRank() - movingCard.getRank() != 1) {
					return new int[] {source, destination};
				}
			}
		}
		throw new AssertionError("Test deal unexpectedly has no illegal cascade move.");
	}

	private static int countCards(FreecellGame game) {
		int count = 0;
		for (int stackNumber = 1; stackNumber <= 16; stackNumber++) {
			count += game.getStack(stackNumber).length;
		}
		return count;
	}

	private static int findAceStack(FreecellGame game) {
		for (int stack = 9; stack <= 16; stack++) {
			Card[] cards = game.getStack(stack);
			if (cards.length > 0 && cards[cards.length - 1].getRank() == 0) {
				return stack;
			}
		}
		return -1;
	}

	private static int[] findLegalMove(FreecellGame game) {
		for (int source = 1; source <= 16; source++) {
			if (source >= 5 && source <= 8) {
				continue;
			}
			Card[] sourceCards = game.getStack(source);
			if (sourceCards.length == 0) {
				continue;
			}
			Card movingCard = sourceCards[sourceCards.length - 1];
			for (int destination = 1; destination <= 16; destination++) {
				if (source == destination) {
					continue;
				}
				Card[] destinationCards = game.getStack(destination);
				if (destinationCards.length == 0) {
					if (destination <= 4 || destination >= 9) {
						return new int[] {source, destination};
					}
					if (destination >= 5 && destination <= 8 && movingCard.getRank() == 0
							&& destination - 5 == movingCard.getSuit()) {
						return new int[] {source, destination};
					}
					continue;
				}
				Card target = destinationCards[destinationCards.length - 1];
				if (destination >= 5 && destination <= 8) {
					if (movingCard.getSuit() == target.getSuit()
							&& movingCard.getRank() == target.getRank() + 1) {
						return new int[] {source, destination};
					}
				} else if (destination >= 9
						&& Card.isRedSuit(movingCard.getSuit()) != Card.isRedSuit(target.getSuit())
						&& target.getRank() - movingCard.getRank() == 1) {
					return new int[] {source, destination};
				}
			}
		}
		throw new AssertionError("Test deal unexpectedly has no legal move.");
	}

	private static int[] findLegalCascadeMove(FreecellGame game) {
		for (int source = 9; source <= 16; source++) {
			Card[] sourceCards = game.getStack(source);
			Card movingCard = sourceCards[sourceCards.length - 1];
			for (int destination = 9; destination <= 16; destination++) {
				if (source == destination) {
					continue;
				}
				Card[] destinationCards = game.getStack(destination);
				Card topCard = destinationCards[destinationCards.length - 1];
				if (Card.isRedSuit(movingCard.getSuit()) != Card.isRedSuit(topCard.getSuit())
						&& topCard.getRank() - movingCard.getRank() == 1) {
					return new int[] {source, destination};
				}
			}
		}
		throw new AssertionError("Test deal unexpectedly has no legal cascade move.");
	}

	private static void assertThrows(Class<? extends Throwable> expectedType,
			ThrowingRunnable action, String description) {
		try {
			action.run();
		} catch (Throwable thrown) {
			if (expectedType.isInstance(thrown)) {
				return;
			}
			throw new AssertionError(description + ": expected " + expectedType.getSimpleName()
					+ ", got " + thrown.getClass().getSimpleName(), thrown);
		}
		throw new AssertionError(description + ": expected " + expectedType.getSimpleName());
	}

	@FunctionalInterface
	private interface ThrowingRunnable {
		void run() throws Exception;
	}
}
