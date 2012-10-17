package de.me.server;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.me.server.listener.Client;
import de.me.server.listener.ClientAcceptListener;
import de.me.server.listener.ClientListener;


public class SimpleServerIntegrationTest {

	private final Logger log = LoggerFactory.getLogger(getClass());


	@Test
	public void testServer() throws Exception {
		final SimpleServer server = new SimpleServer(8181);

		server.addListener(new ClientAcceptListener() {

			@Override
			public void onIncomingClient(final Client client) {
				try {
					log.info("Have new client from {}", client.getRemoteAddress());
				}
				catch (IOException e) {
					log.info("Cannot get remote address");
				}

				client.addListener(new ClientListener() {

					@Override
					public void onMessage(ByteBuffer messageBuffer) {
						log.info("onMessage: {}", messageBuffer);

						try {
							ByteBuffer buf = ByteBuffer.allocateDirect(messageBuffer.limit() + 100);
							buf.put(("Hello you from "+client.getRemoteAddress()+"\n\nYou sent me:\n").getBytes());
							buf.put(messageBuffer);
							buf.flip();

							client.send(buf);

							//client.close();

							//server.stop();
						}
						catch (IOException e) {
							log.error("Write error", e);
						}
					}

					@Override
					public void onError(Throwable exception) {
						log.error("onError", exception);
					}

					@Override
					public void onClose() {
						log.info("Client closed");
					}

				});

				client.addListener(new ClientListener() {

					@Override
					public void onMessage(ByteBuffer buffer) {
						log.info("onRead2: {}", buffer);

						try {
							ByteBuffer buf = ByteBuffer.allocateDirect(buffer.limit() + 100);
							buf.put(("[2] Hello you from "+client.getRemoteAddress()+"\n\nYou sent me:\n").getBytes());
							buf.put(buffer);
							buf.flip();

							client.send(buf);

							client.close();

							server.stop();
						}
						catch (IOException e) {
							log.error("Write error 2", e);
						}
					}

					@Override
					public void onError(Throwable e) {
						log.error("onError 2", e);
					}

					@Override
					public void onClose() {
						log.info("Client closed 2");
					}

				});
			}

		});

		log.debug("Start server");
		server.start();
		log.debug("Server finished");
	}

}
