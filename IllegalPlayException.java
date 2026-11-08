public class IllegalPlayException extends Exception {
	IllegalPlayException() {
		this("That card stack cannot be played from.");
	}

	public IllegalPlayException(String message) {
		super(message);
	}
}
