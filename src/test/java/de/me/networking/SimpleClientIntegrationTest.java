package de.me.networking;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.me.networking.client.SimpleClient;
import de.me.networking.client.listener.ClientListener;


public class SimpleClientIntegrationTest {

	private final Logger log = LoggerFactory.getLogger(getClass());


	@Test
	public void testClient() throws Exception {
		final SimpleClient client = new SimpleClient("www.google.de", 80);

		client.addListener(new ClientListener() {

			@Override
			public void onConnect() {
				log.info("Client connected");

				ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
				buffer.put("GET / HTTP/1.0\r\nHost: www.google.de\r\nConnection: close\r\n\r\n".getBytes());
				buffer.flip();

				try {
					client.send(buffer);
				}
				catch (IOException e) {
					log.error("Write error", e);
				}
			}

			@Override
			public void onMessage(ByteBuffer messageBuffer) {
				log.info("onMessage: {}", messageBuffer);

				//TODO
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

		client.connect();
	}

}
