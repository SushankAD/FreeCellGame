
public class IllegalPlayExceptionTest {
	public static void main(String[] args) {
		IllegalPlayException defaultException = new IllegalPlayException();
		if ("That card stack cannot be played from.".equals(defaultException.getMessage())) {
			System.out.println("pass: default exception message");
		} else {
			System.out.println("fail: default exception message");
			System.exit(1);
		}

		String message = "Test exception message";
		IllegalPlayException customException = new IllegalPlayException(message);
		if (message.equals(customException.getMessage())) {
			System.out.println("pass: custom exception message");
		} else {
			System.out.println("fail: custom exception message");
			System.exit(1);
		}
	}
}
