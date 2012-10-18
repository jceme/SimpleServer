package de.me.networking.client.listener;




/**
 * A listener extending the server {@link de.me.networking.server.listener.ClientListener} notifying on connect.
 */
public interface ClientListener extends de.me.networking.server.listener.ClientListener {

	/**
	 * Notification that the client connected.
	 */
	public void onConnect();

}
