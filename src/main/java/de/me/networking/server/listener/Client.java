package de.me.networking.server.listener;




/**
 * A facade for a client accepted by the server.
 */
public interface Client extends ClientBase {

	/**
	 * Adds a new listener for the client.<br>
	 * <b>Note</b> that listeners should be added immediately when receiving the incoming notification.
	 */
	public Client addListener(ClientListener listener);

}
