import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class FreecellApi {
	private FreecellGame game = new FreecellGame(new Random().nextLong());
	private int moveCount;
	private long startedAt = System.currentTimeMillis();

	public record HealthResponse(String status) {}

	public record NewGameRequest(Long seed) {}

	public record MoveRequest(int source, int destination) {}

	public record CardResponse(
			int rank,
			int suit,
			String rankName,
			String suitName,
			boolean red,
			String image
	) {}

	public record StackResponse(
			int number,
			String type,
			List<CardResponse> cards
	) {}

	public record GameResponse(
			List<StackResponse> stacks,
			int moveCount,
			boolean gameOver,
			boolean canUndo,
			long elapsedSeconds
	) {}

	public record MessageResponse(String message) {}

	@GetMapping("/health")
	public HealthResponse health() {
		return new HealthResponse("ok");
	}

	@GetMapping("/game")
	public GameResponse getGame() {
		return toGameResponse();
	}

	@PostMapping("/game/new")
	public GameResponse newGame(@RequestBody(required = false) NewGameRequest request) {
		long seed = request != null && request.seed() != null
				? request.seed()
				: new Random().nextLong();
		game = new FreecellGame(seed);
		moveCount = 0;
		startedAt = System.currentTimeMillis();
		return toGameResponse();
	}

	@PostMapping("/game/move")
	public GameResponse move(@RequestBody MoveRequest request) {
		try {
			boolean moved = game.play(request.source(), request.destination());
			if (!moved) {
				throw new IllegalMoveException("That move is not legal.");
			}
			moveCount++;
			return toGameResponse();
		} catch (IllegalPlayException exception) {
			throw new IllegalMoveException(exception.getMessage());
		}
	}

	@PostMapping("/game/undo")
	public GameResponse undo() {
		if (!game.undo()) {
			throw new IllegalMoveException("There is no move to undo.");
		}
		moveCount = Math.max(0, moveCount - 1);
		return toGameResponse();
	}

	private GameResponse toGameResponse() {
		List<StackResponse> stacks = new ArrayList<>();
		for (int number = 1; number <= 16; number++) {
			List<CardResponse> cards = new ArrayList<>();
			for (Card card : game.getStack(number)) {
				cards.add(toCardResponse(card));
			}
			stacks.add(new StackResponse(number, stackType(number), cards));
		}
		long elapsedSeconds = game.isGameOver()
				? (System.currentTimeMillis() - startedAt) / 1000
				: (System.currentTimeMillis() - startedAt) / 1000;
		return new GameResponse(
				stacks,
				moveCount,
				game.isGameOver(),
				game.canUndo(),
				elapsedSeconds
		);
	}

	private static String stackType(int number) {
		if (number <= 4) {
			return "cell";
		}
		if (number <= 8) {
			return "foundation";
		}
		return "cascade";
	}

	private static CardResponse toCardResponse(Card card) {
		int suit = card.getSuit();
		String suitCode = switch (suit) {
			case 0 -> "C";
			case 1 -> "D";
			case 2 -> "H";
			default -> "S";
		};
		String rankName = Card.getRankName(card.getRank());
		String suitName = Card.getSuitName(suit);
		return new CardResponse(
				card.getRank(),
				suit,
				rankName,
				suitName,
				Card.isRedSuit(suit),
				rankName + suitCode + ".png"
		);
	}

	private static class IllegalMoveException extends RuntimeException {
		private IllegalMoveException(String message) {
			super(message);
		}
	}

	@ExceptionHandler(IllegalMoveException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public MessageResponse handleIllegalMove(IllegalMoveException exception) {
		return new MessageResponse(exception.getMessage());
	}
}
