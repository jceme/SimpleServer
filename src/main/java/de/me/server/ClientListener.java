package de.me.server;

import java.nio.ByteBuffer;


public interface ClientListener {

	public void onRead(ByteBuffer buffer);

	public void onClose();

	public void onError(Throwable e);

}
