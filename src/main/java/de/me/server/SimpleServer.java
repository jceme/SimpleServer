package de.me.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.me.server.listener.ClientAcceptListener;


/**
 * A simple event-driving server.
 */
public class SimpleServer {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private SocketAddress socketAddress;

	private int corePoolSize = 2;
	private int maxPoolSize = Integer.MAX_VALUE;
	private long poolTimeout = 60;
	private int clientBufferCapacity = 4096;
	private Executor executor = null;

	private final List<ClientAcceptListener> listeners = new LinkedList<>();

	private ServerSocketChannel server;


	/**
	 * Create new {@link SimpleServer} listening on port 8181 on all interfaces.
	 */
	public SimpleServer() {
		this(8181);
	}

	/**
	 * Create new {@link SimpleServer} listening on provided port on all interfaces.
	 */
	public SimpleServer(int port) {
		this(null, port);
	}

	/**
	 * Create new {@link SimpleServer} listening on provided port on interface for provided host or all interfaces.
	 * @param host The hostname to listen on or <code>null</code> to listen on all interfaces
	 */
	public SimpleServer(String host, int port) {
		this(host == null ? new InetSocketAddress(port) : new InetSocketAddress(host, port));
	}

	/**
	 * Create new {@link SimpleServer} listening on provided socket address.
	 */
	public SimpleServer(SocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}


	/**
	 * Starts and executes the server.<br>
	 * This method will block until the server was shut-down by {@link #stop()}.
	 */
	public void start() throws IOException {
		if (server != null) {
			throw new IllegalStateException("Server already started");
		}

		if (listeners.isEmpty()) {
			log.warn("No listeners registered yet to handle client connections");
		}
		final Executor executor;
		final boolean ownExecutor;
		if (this.executor == null) {
			ownExecutor = true;
			executor = new ThreadPoolExecutor(
					corePoolSize, maxPoolSize,
	                poolTimeout, TimeUnit.SECONDS,
	                new SynchronousQueue<Runnable>());
			log.debug("Created new executor {}", executor);
		}
		else {
			ownExecutor = false;
			executor = this.executor;
			log.debug("Using provided executor {}", executor);
		}


		try {
			log.debug("Starting server");

			server = ServerSocketChannel.open().bind(socketAddress);
			log.info("Server listening on {}", socketAddress);

			try {
				for (;;) {
					final SocketChannel client = server.accept();
					if (log.isDebugEnabled()) {
						log.debug("Accepted new client from {}", client.getRemoteAddress());
					}

					for (ClientAcceptListener listener : listeners) {
						executor.execute(new ClientAcceptHandler(client, listener, clientBufferCapacity));
					}
				}
			}
			finally {
				if (server.isOpen()) {
					log.debug("Closing server channel");
					server.close();
				}
			}
		}
		catch (AsynchronousCloseException e) {
		}
		catch (Exception e) {
			log.error("Server error", e);
			throw e;
		}
		finally {
			if (ownExecutor) {
				log.debug("Shutting down created executor");
				((ThreadPoolExecutor) executor).shutdown();
			}
		}

		log.info("Server shut down");
	}


	/**
	 * Stops the server previously started with {@link #start()}.
	 */
	public void stop() throws IOException {
		if (server == null) {
			throw new IllegalStateException("Server not running");
		}

		log.debug("Stopping server");
		server.close();
		log.debug("Server stopped");
	}


	public SimpleServer setSocketAddress(SocketAddress socketAddress) {
		this.socketAddress = socketAddress;
		return this;
	}

	public SimpleServer setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
		return this;
	}

	public SimpleServer setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
		return this;
	}

	public SimpleServer setPoolTimeout(long poolTimeout) {
		this.poolTimeout = poolTimeout;
		return this;
	}

	/**
	 * Sets the {@link ByteBuffer} capacity to use for client handlers.
	 */
	public SimpleServer setClientBufferCapacity(int clientBufferCapacity) {
		this.clientBufferCapacity = clientBufferCapacity;
		return this;
	}

	/**
	 * Sets the executor to use when a new client is to be handled.
	 */
	public SimpleServer setExecutor(Executor executor) {
		this.executor = executor;
		return this;
	}

	/**
	 * Adds a {@link ClientAcceptListener}.<br>
	 * <b>Note</b> that listeners should be added before calling {@link #start()}.
	 */
	public SimpleServer addListener(ClientAcceptListener listener) {
		if (listener == null) throw new IllegalArgumentException("Listener required");
		listeners.add(listener);
		return this;
	}

}
