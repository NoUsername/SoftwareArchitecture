package at.fhooe.mcm441.commons.protocol;

import at.fhooe.mcm441.commons.network.Client;

public interface IServerCommandListener {
	
	public void onRegisterForSensor(Client c,  String sensorId, boolean status);
	
	public void onAdminConfigCommand(Client c, Object conf, Object data);
	
	public void onAdminSensorConfigCommand(Client c, String sensorId, Object conf, Object data);
	
	public void onByeMessage(Client c);
	
}
