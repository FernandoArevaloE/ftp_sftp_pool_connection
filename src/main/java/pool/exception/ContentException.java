package pool.exception;

public class ContentException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ContentException(String message) {
		super(message);
	}

	public ContentException(String message, Throwable cause) {
		super(message, cause);
	}

}
