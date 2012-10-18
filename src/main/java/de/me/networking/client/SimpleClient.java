package de.me.networking.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.me.networking.client.listener.Client;
import de.me.networking.client.listener.ClientListener;
import de.me.networking.server.ClientHandler;
import de.me.networking.server.ListenerException;
import de.me.networking.server.listener.ClientAcceptListener;


/**
 * A simple event-driving client.
 */
public class SimpleClient implements Client {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private int clientBufferCapacity = 4096;

	private InetSocketAddress socketAddress;
	private final List<ClientListener> listeners = new LinkedList<>();
	private ClientHandler handler;


	/**
	 * Create new {@link SimpleClient} prepared to connect to the localhost on provided port.
	 */
	public SimpleClient(int port) {
		this("localhost", port);
	}

	/**
	 * Create new {@link SimpleClient} prepared to connect to host on port.
	 */
	public SimpleClient(String host, int port) {
		this(new InetSocketAddress(host, port));
	}

	/**
	 * Create new {@link SimpleClient} prepared to connect to the provided socket address.
	 */
	public SimpleClient(InetSocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}


	/**
	 * Actually connects this client to the configured socket address.<br>
	 * This method will <b>block</b> until the client was completely handled.<br><br>
	 * <b>Note</b> that listeners should be added <u>before</u> by calling {@link #addListener(ClientAcceptListener)}.
	 *
	 * @throws IOException If the connection process failed
	 */
	public void connect() throws IOException, ListenerException {
		if (handler != null) throw new IllegalStateException("Client already in use");

		handler = new ClientHandler(SocketChannel.open(socketAddress), clientBufferCapacity);

		for (ClientListener listener : listeners) {
			handler.addListener(listener);
		}

		onConnect();

		try {
			handler.execute();
		}
		catch (Throwable e) {
			log.error("Client Handler error", e);

			// Inform listeners for error
			try {
				handler.onError(e);
			}
			catch (Throwable ee) {
				log.error("Error in client handler onError", ee);
			}
		}
	}


	@Override
	public SimpleClient addListener(ClientListener listener) {
		if (listener == null) throw new IllegalArgumentException("Listener required");

		if (handler == null) {
			listeners.add(listener);
		}
		else {
			handler.addListener(listener);
		}

		return this;
	}

	@Override
	public int getBufferCapacity() {
		return clientBufferCapacity;
	}

	@Override
	public void suspendMessages() {
		checkHandler();
		handler.suspendMessages();
	}

	@Override
	public void resumeMessages() {
		checkHandler();
		handler.resumeMessages();
	}

	@Override
	public int send(ByteBuffer buffer) throws IOException {
		checkHandler();
		return handler.send(buffer);
	}

	@Override
	public void close() throws IOException {
		checkHandler();
		handler.close();
	}

	@Override
	public SocketAddress getRemoteAddress() throws IOException {
		checkHandler();
		return handler.getRemoteAddress();
	}

	@Override
	public boolean isOpen() {
		checkHandler();
		return handler.isOpen();
	}


	/**
	 * Overwrites the socket address provided in the constructor.
	 */
	public SimpleClient setSocketAddress(InetSocketAddress socketAddress) {
		this.socketAddress = socketAddress;
		return this;
	}

	/**
	 * Sets the client buffer capacity, defaults to <code>4096</code>.
	 */
	public SimpleClient setClientBufferCapacity(int clientBufferCapacity) {
		this.clientBufferCapacity = clientBufferCapacity;
		return this;
	}


	private void onConnect() throws ListenerException {
		for (ClientListener listener : listeners) {
			try {
				listener.onConnect();
			}
			catch (Throwable e) {
				throw new ListenerException(e);
			}
		}
	}

	private void checkHandler() {
		if (handler == null) throw new IllegalStateException("Client not connected yet");
	}

}
