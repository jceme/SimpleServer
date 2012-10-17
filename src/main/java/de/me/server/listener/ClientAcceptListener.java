package de.me.server.listener;

import de.me.server.Client;


public interface ClientAcceptListener {

	public void onIncomingClient(Client client);

}
