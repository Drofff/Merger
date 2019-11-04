package drofff.merger.exception;

public class MergerException extends RuntimeException {
	public MergerException() {
		super();
	}

	public MergerException(String message) {
		super(message);
	}

	public MergerException(String message, Throwable cause) {
		super(message, cause);
	}

	public MergerException(Throwable cause) {
		super(cause);
	}

	protected MergerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
