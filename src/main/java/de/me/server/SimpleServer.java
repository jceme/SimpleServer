package de.me.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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


public class SimpleServer {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private SocketAddress socketAddress;

	private int corePoolSize = 2;
	private int maxPoolSize = Integer.MAX_VALUE;
	private long poolTimeout = 60;
	private Executor executor = null;

	private List<Void> listeners = new LinkedList<>();

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
			server = ServerSocketChannel.open().bind(socketAddress);
			log.info("Server listening on {}", socketAddress);

			try {
				for (;;) {
					final SocketChannel client = server.accept();
					if (log.isDebugEnabled()) {
						log.debug("Accepted new client from {}", client.getRemoteAddress());
					}

					//executor.execute(new ClientHandler(client));
				}
			}
			finally {
				log.debug("Closing server channel");
				server.close();
			}
		}
		finally {
			if (ownExecutor) {
				log.debug("Shutting down created executor");
				((ThreadPoolExecutor) executor).shutdown();
			}
		}
	}



	public static void main(String[] args) throws Exception {
		new SimpleServer().start();
	}

}
