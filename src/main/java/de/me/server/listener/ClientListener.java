package de.me.server.listener;

import java.nio.ByteBuffer;


/**
 * A listener for client events.
 */
public interface ClientListener {

	/**
	 * Notification that the client sent some data.
	 *
	 * @param buffer the <b>read-only</b> buffer containing the data
	 */
	public void onMessage(ByteBuffer messageBuffer);

	/**
	 * Notification that the client closed the connection.<br>
	 * <b>Note</b> that this does not notify server-side closes as with {@link Client#close()}!
	 */
	public void onClose();

	/**
	 * Notification that some exception happened when dealing with the client.<br><br>
	 * The client connection may have been closed, can be checked with {@link Client#isOpen()}.<br><br>
	 * The connection is closed after all listeners have been notified.
	 *
	 * @param exception the exception that occurred
	 */
	public void onError(Throwable exception);

}
