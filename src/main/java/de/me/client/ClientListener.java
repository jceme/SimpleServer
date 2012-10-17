package de.me.client;




/**
 * A listener extending the server {@link de.me.server.listener.ClientListener} notifying on connect.
 */
public interface ClientListener extends de.me.server.listener.ClientListener {

	/**
	 * Notification that the client connected.
	 */
	public void onConnect();

}
