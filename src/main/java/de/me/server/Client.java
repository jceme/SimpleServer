package de.me.server;

import java.io.IOException;
import java.nio.ByteBuffer;


public interface Client {

	public Client addListener(ClientListener listener);

	public int write(ByteBuffer buffer) throws IOException;

	public void close() throws IOException;

}
