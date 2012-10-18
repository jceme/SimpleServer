package de.me.networking;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.me.networking.client.SimpleClient;
import de.me.networking.client.listener.ClientListener;


public class SimpleClientIntegrationTest {

	private final Logger log = LoggerFactory.getLogger(getClass());


	@Test
	public void testClient() throws Exception {
		final SimpleClient client = new SimpleClient("www.google.de", 80).setClientBufferCapacity(200);

		client.addListener(new ClientListener() {

			private int msgcnt = 0;
			private long bytecnt = 0L;

			@Override
			public void onConnect() {
				log.info("Client connected");

				ByteBuffer buffer = ByteBuffer.allocateDirect(200);
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
				bytecnt += messageBuffer.limit();
				log.info("onMessage [{}]: {}", ++msgcnt, messageBuffer);

				if (msgcnt == 5) {
					log.info("Suspending client messages for 3 seconds");
					client.suspendMessages();

					new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							log.info("Resuming client messages");
							client.resumeMessages();
						}
					}, 3000L);
				}
			}

			@Override
			public void onError(Throwable exception) {
				log.error("onError", exception);
			}

			@Override
			public void onClose() {
				log.info("Client closed, bytes received: {}", bytecnt);
			}
		});

		client.connect();
	}

}
