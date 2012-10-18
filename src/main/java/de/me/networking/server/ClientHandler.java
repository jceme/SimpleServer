package de.me.networking.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.me.networking.server.listener.Client;
import de.me.networking.server.listener.ClientListener;


/**
 * Internal {@link Client} facade implementation.
 */
public class ClientHandler implements Client {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final SocketChannel client;
	private final List<ClientListener> listeners = new LinkedList<>();
	private final int capacity;

	private boolean suspendRead = false;


	public ClientHandler(SocketChannel client, int capacity) {
		this.client = client;
		this.capacity = capacity;
	}

	@Override
	public Client addListener(ClientListener listener) {
		if (listener == null) throw new IllegalArgumentException("Listener required");
		listeners.add(listener);
		return this;
	}

	@Override
	public int getBufferCapacity() {
		return capacity;
	}

	@Override
	public void suspendMessages() {
		suspendRead = true;
	}

	@Override
	public void resumeMessages() {
		if (suspendRead) {
			try {
				readClientInput(ByteBuffer.allocateDirect(capacity));
			}
			catch (IOException e) {
				onError(e);
			}
			finally {
				suspendRead = false;
			}
		}
	}


	@Override
	public int send(ByteBuffer buffer) throws IOException {
		return client.write(buffer);
	}


	@Override
	public void close() throws IOException {
		try {
			client.shutdownOutput();
		}
		catch (ClosedChannelException e) {}

		try {
			client.shutdownInput();
		}
		catch (ClosedChannelException e) {}

		try {
			client.close();
		}
		catch (ClosedChannelException e) {}
	}


	/**
	 * Starts handling client traffic.
	 */
	public void execute() throws Exception {
		client.configureBlocking(false);

		final Selector selector = Selector.open();
		final SelectionKey selkey = client.register(selector, client.validOps());

		final ByteBuffer buffer = ByteBuffer.allocateDirect(capacity);

		for (;;) {
			selector.select();

			if (log.isTraceEnabled()) {
				log.trace("Have selection ops {}", selkey.readyOps());
			}

			if (selkey.isReadable()) {
				log.trace("Client signalized readability");

				if (suspendRead) {
					log.trace("Client read currently suspended");
				}
				else if (readClientInput(buffer)) {
					// Have EOF
					break;
				}
			}

			if (log.isTraceEnabled()) {
				if (selkey.isConnectable()) {
					log.trace("Client signalized connectability");
				}

				if (selkey.isWritable()) {
					log.trace("Client signalized writability");
				}
			}
		}
	}

	private boolean readClientInput(final ByteBuffer buffer) throws IOException {
		buffer.clear();

		int r = client.read(buffer);

		if (r < 0) {
			log.debug("Client read signalized EOF");

			onClose();
			return true;
		}
		else if (r > 0) {
			log.trace("Client read message of length {}", r);

			buffer.flip();
			onMessage(buffer);
		}

		return false;
	}


	@Override
	public SocketAddress getRemoteAddress() throws IOException {
		return client.getRemoteAddress();
	}

	@Override
	public boolean isOpen() {
		return client.isOpen();
	}


	private void onMessage(ByteBuffer buffer) throws ListenerException {
		final ByteBuffer robuffer = buffer.asReadOnlyBuffer();

		for (ClientListener listener : listeners) {
			try {
				robuffer.rewind();
				listener.onMessage(robuffer);
			}
			catch (Throwable e) {
				throw new ListenerException(e);
			}
		}
	}

	private void onClose() throws ListenerException {
		for (ClientListener listener : listeners) {
			try {
				listener.onClose();
			}
			catch (Throwable e) {
				throw new ListenerException(e);
			}
		}
	}

	public void onError(Throwable e) throws ListenerException {
		for (ClientListener listener : listeners) {
			try {
				listener.onError(e);
			}
			catch (Throwable ee) {
				throw new ListenerException(ee);
			}
		}

		// Try to close the client connection
		try {
			close();
		}
		catch (IOException ee) {
			log.error("Failed to close client onError", ee);
		}
	}

}
