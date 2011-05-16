package at.fhooe.mcm441.commons.protocol;

public interface IAdminClientCommands extends IClientCommands {
	
	public void setConfiguration(String confId, String confData);
	public void setSensorConfiguration(String sensorid, String confId, String confData);

}
