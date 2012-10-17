package de.me.server.listener;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;


/**
 * A facade base for a client.
 */
public interface ClientBase {

	/**
	 * Sends the buffer content as is to the client.
	 */
	public int send(ByteBuffer buffer) throws IOException;

	/**
	 * Closes the client connection.
	 */
	public void close() throws IOException;

	/**
	 * Gets the client's remote address.
	 */
	public SocketAddress getRemoteAddress() throws IOException;

	/**
	 * Checks if the client connection is still open.
	 *
	 * @return true, if it is open
	 */
	public boolean isOpen();

}
