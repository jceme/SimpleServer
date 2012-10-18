package de.me.networking.client.listener;

import de.me.networking.server.listener.ClientBase;



/**
 * A facade for a simple client.
 */
public interface Client extends ClientBase {

	/**
	 * Adds a new listener for the client.<br>
	 * <b>Note</b> that listeners should be added immediately when receiving the incoming notification.
	 */
	public Client addListener(ClientListener listener);

}
