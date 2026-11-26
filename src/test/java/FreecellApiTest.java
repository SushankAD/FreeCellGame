import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FreecellApiTest {
	@Test
	void healthEndpointReturnsOk() {
		FreecellApi api = new FreecellApi();
		assertEquals("ok", api.health().status());
	}

	@Test
	void createsGameWithSixteenStacks() {
		FreecellApi api = new FreecellApi();
		FreecellApi.GameResponse response = api.newGame(new FreecellApi.NewGameRequest(42L));
		assertEquals(16, response.stacks().size());
		assertEquals(0, response.moveCount());
		assertTrue(response.stacks().stream().allMatch(stack -> stack.cards() != null));
	}

	@Test
	void rejectsInvalidMove() {
		FreecellApi api = new FreecellApi();
		assertThrows(RuntimeException.class, () ->
				api.move(new FreecellApi.MoveRequest(1, 20)));
	}
}
