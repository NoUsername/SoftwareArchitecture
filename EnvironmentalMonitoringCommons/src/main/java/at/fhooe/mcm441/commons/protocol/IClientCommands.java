package at.fhooe.mcm441.commons.protocol;

/**
 * commands a client can invoke on the server
 * 
 * @author Paul Klingelhuber
 *
 */
public interface IClientCommands {
	
	/**
	 * register or unregister for a server
	 * @param sensorId
	 * @param registered false to unregister
	 */
	public void setRegistrationForSensor(String sensorId, boolean registered);
	/**
	 * if you are nice, call this when you shut down the client
	 */
	public void logoff();

}
