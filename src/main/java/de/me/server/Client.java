package de.me.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import de.me.server.listener.ClientListener;


public interface Client {

	public Client addListener(ClientListener listener);

	public int write(ByteBuffer buffer) throws IOException;

	public void close() throws IOException;

	public SocketAddress getRemoteAddress() throws IOException;

	public boolean isOpen();

}
