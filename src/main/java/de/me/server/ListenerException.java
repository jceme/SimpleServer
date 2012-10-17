package de.me.server;


public class ListenerException extends Exception {

	private static final long serialVersionUID = 7896036116577290373L;


	public ListenerException() {
	}

	public ListenerException(String message) {
		super(message);
	}

	public ListenerException(Throwable cause) {
		super(cause);
	}

	public ListenerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ListenerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
