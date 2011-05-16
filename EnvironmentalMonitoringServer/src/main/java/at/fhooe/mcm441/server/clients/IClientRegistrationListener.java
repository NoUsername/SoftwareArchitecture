package at.fhooe.mcm441.server.clients;

import at.fhooe.mcm441.commons.network.Client;

/**
 * can be used to react to changes in the connected
 * clients (to inform admin clients about registered/deregistered clients)
 * 
 * @author Paul Klingelhuber
 */
public interface IClientRegistrationListener {
	
	/**
	 * will be called when a client registers or de-registers
	 * 
	 * @param c
	 * @param registered if true, he reigstered, else he de-registered
	 */
	public void onClientRegistrationChanged(Client c, boolean registered);

}
