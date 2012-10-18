package de.me.networking.server.listener;



/**
 * An interface for notifications on new clients.
 */
public interface ClientAcceptListener {

	/**
	 * Notification that a new client was accepted by the server.<br>
	 * <b>Note:</b> Implementations should register their listeners with
	 * {@link Client#addListener(ClientListener)} in this method.
	 *
	 * @param client the accepted client (facade)
	 */
	public void onIncomingClient(Client client);

}
