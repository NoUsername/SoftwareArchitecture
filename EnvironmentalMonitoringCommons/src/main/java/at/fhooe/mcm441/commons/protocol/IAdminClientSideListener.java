package at.fhooe.mcm441.commons.protocol;

import at.fhooe.mcm441.commons.Configuration;
import at.fhooe.mcm441.commons.network.Client;

/**
 * All messaages an adminclient can get from the server
 * 
 * these are IN ADDITION to the normal client messages of course!
 * 
 * @author Paul Klingelhuber
 */
public interface IAdminClientSideListener extends IClientSideListener {
	
	/**
	 * configuration item for a sensor
	 * @param sensorId
	 * @param conf
	 */
	public void onSensorConfigurationItem(String sensorId, Configuration conf);
	/**
	 * configuration item for the server
	 * @param conf
	 */
	public void onServerConfigurationItem(Configuration conf);
	
	/**
	 * a new client connected
	 * @param client
	 */
	public void onClientConnected(Client client);
	/**
	 * a client disconnected
	 * @param client
	 */
	public void onClientDisconnected(Client client);
}
