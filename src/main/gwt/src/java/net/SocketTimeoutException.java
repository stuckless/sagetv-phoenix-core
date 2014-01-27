package java.net;

public class SocketTimeoutException extends Exception {
	public SocketTimeoutException() {
		super();
	}

	public SocketTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public SocketTimeoutException(String message) {
		super(message);
	}

	public SocketTimeoutException(Throwable cause) {
		super(cause);
	}
}
