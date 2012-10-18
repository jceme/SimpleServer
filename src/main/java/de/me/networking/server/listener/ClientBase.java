package de.me.networking.server.listener;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;


/**
 * A facade base for a client.
 */
public interface ClientBase {

	/**
	 * Gets the read buffer capacity.
	 */
	public int getBufferCapacity();

	/**
	 * Sends the buffer content as is to the client.
	 */
	public int send(ByteBuffer buffer) throws IOException;

	/**
	 * Prevents reading more client input.<br>
	 * Can be undone with {@link #resumeMessages()}.<br><br>
	 * This is useful if no more {@link ClientListener#onMessage(java.nio.ByteBuffer)} can be accepted
	 * due to full buffers.
	 */
	public void suspendMessages();

	/**
	 * Resumes reading client input previously suspended with {@link #suspendMessages()}.
	 */
	public void resumeMessages();

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
