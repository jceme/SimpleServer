package de.me.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SimpleServer {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private SocketAddress socketAddress;

	private int corePoolSize = 2;
	private int maxPoolSize = Integer.MAX_VALUE;
	private long poolTimeout = 60;
	private Executor executor = null;

	private final List<ClientAcceptListener> listeners = new LinkedList<>();

	private ServerSocketChannel server;


	public SimpleServer() {
		this(8181);
	}

	public SimpleServer(int port) {
		this(null, port);
	}

	public SimpleServer(String host, int port) {
		this(host == null ? new InetSocketAddress(port) : new InetSocketAddress(host, port));
	}

	public SimpleServer(SocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}


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
						executor.execute(new ClientAcceptHandler(client, listener));
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


	public void stop() throws IOException {
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

	public SimpleServer setExecutor(Executor executor) {
		this.executor = executor;
		return this;
	}

	public SimpleServer addListener(ClientAcceptListener listener) {
		if (listener == null) throw new IllegalArgumentException("Listener required");
		listeners.add(listener);
		return this;
	}



	public static void main(String[] args) throws Exception {
		final AtomicReference<SimpleServer> serverref = new AtomicReference<>();

		// Stop server after 10 seconds
		final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
		exec.schedule(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				serverref.get().stop();
				exec.shutdown();
				return null;
			}
		}, 10, TimeUnit.SECONDS);

		SimpleServer server = new SimpleServer();
		serverref.set(server);

		server.start();
	}

}
