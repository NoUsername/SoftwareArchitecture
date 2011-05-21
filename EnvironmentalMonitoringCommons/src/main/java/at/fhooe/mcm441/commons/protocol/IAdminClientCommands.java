package at.fhooe.mcm441.commons.protocol;

/**
 * commands that the client can trigger on the server
 * 
 * @author Paul Klingelhuber
 */
public interface IAdminClientCommands extends IClientCommands {
	/**
	 * configuration should be set
	 * 
	 * @param confId what should be configured
	 * @param confData new value
	 */
	void setConfiguration(String confId, String confData);
	
	/**
	 * configuration for a sensor should be set
	 * 
	 * @param sensorid
	 * @param confId what should be configured
	 * @param confData new value
	 */
	void setSensorConfiguration(String sensorid, String confId, String confData);

}
