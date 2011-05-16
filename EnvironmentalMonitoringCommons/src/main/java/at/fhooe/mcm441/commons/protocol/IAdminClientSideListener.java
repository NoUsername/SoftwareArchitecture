package at.fhooe.mcm441.commons.protocol;

import at.fhooe.mcm441.commons.Configuration;
import at.fhooe.mcm441.commons.network.Client;

public interface IAdminClientSideListener extends IClientSideListener {
	
	public void onSensorConfigurationItem(String sensorId, Configuration conf);
	public void onServerConfigurationItem(Configuration conf);
	
	public void onClientConnected(Client client);
	public void onClientDisconnected(Client client);

}
