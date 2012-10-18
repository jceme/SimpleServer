SimpleServer
============

A simple network server and client implementation using event-driven style.

SimpleServer
------------

```java
SimpleServer server = new SimpleServer(8080);

server.addListener(new ClientAcceptListener() {

	@Override
	public void onIncomingClient(final Client client) {
		client.addListener(new ClientListener() {

			@Override
			public void onMessage(ByteBuffer messageBuffer) {
				// Note that messageBuffer is a read-only ByteBuffer!
				
				try {
					ByteBuffer buf = ByteBuffer.allocateDirect(messageBuffer.limit() + 100);
					buf.put(("Hello you from " + client.getRemoteAddress() + "\n\nYou sent me:\n").getBytes());
					buf.put(messageBuffer);
					buf.flip();

					client.send(buf);
				}
				catch (IOException e) {
					// Write error
				}
			}

			@Override
			public void onError(Throwable exception) {
				// Connection error
			}

			@Override
			public void onClose() {
				// Client closed connection
			}

		});

	}

});

server.start();
```

SimpleClient
------------

```java
final SimpleClient client = new SimpleClient("www.example.com", 80);

client.addListener(new ClientListener() {

	@Override
	public void onConnect() {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
		buffer.put("GET / HTTP/1.0\r\nHost: www.example.com\r\nConnection: close\r\n\r\n".getBytes());
		buffer.flip();

		try {
			client.send(buffer);
		}
		catch (IOException e) {
			// Write error
		}
	}

	@Override
	public void onMessage(ByteBuffer messageBuffer) {
		// Received new message from server

		// Note that messageBuffer is a read-only ByteBuffer!
	}

	@Override
	public void onError(Throwable exception) {
		// Connection error
	}

	@Override
	public void onClose() {
		// Server closed connection
	}

});

client.connect();
```
