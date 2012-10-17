package de.me.server.listener;

import java.nio.ByteBuffer;


public interface ClientListener {

	public void onRead(ByteBuffer buffer);

	public void onClose();

	public void onError(Throwable e);

}
