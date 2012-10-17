package de.me.server;

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

import de.me.server.listener.ClientListener;


class ClientHandler implements Client {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final SocketChannel client;
	private final List<ClientListener> listeners = new LinkedList<>();
	private final int capacity;


	ClientHandler(SocketChannel client, int capacity) {
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
	public int write(ByteBuffer buffer) throws IOException {
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


	void execute() throws Exception {
		client.configureBlocking(false);

		final Selector selector = Selector.open();
		final SelectionKey selkey = client.register(selector, client.validOps());

		final ByteBuffer buffer = ByteBuffer.allocateDirect(capacity);

		for (;;) {
			if (selector.select() > 0) {
				if (selkey.isReadable()) {
					buffer.clear();

					int r = client.read(buffer);

					if (r < 0) {
						onClose();
						break;
					}
					else if (r > 0) {
						buffer.flip();
						onRead(buffer);
					}
				}
			}
		}
	}


	@Override
	public SocketAddress getRemoteAddress() throws IOException {
		return client.getRemoteAddress();
	}

	@Override
	public boolean isOpen() {
		return client.isOpen();
	}


	private void onRead(ByteBuffer buffer) throws ListenerException {
		final ByteBuffer robuffer = buffer.asReadOnlyBuffer();

		for (ClientListener listener : listeners) {
			try {
				listener.onRead(robuffer);
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

	void onError(Throwable e) throws ListenerException {
		for (ClientListener listener : listeners) {
			try {
				listener.onError(e);
			}
			catch (Throwable ee) {
				throw new ListenerException(ee);
			}
		}

		try {
			close();
		}
		catch (IOException ee) {
			log.error("Failed to close client onError", ee);
		}
	}

}
