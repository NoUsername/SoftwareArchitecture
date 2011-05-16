package at.fhooe.mcm441.commons.protocol;


public interface IClientCommands {
	
	public void setRegistrationForSensor(String sensorId, boolean registered);
	public void logoff();

}
