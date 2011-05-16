package at.fhooe.mcm441.commons.protocol;

import at.fhooe.mcm441.sensor.Sensor;

public interface IClientSideListener {
	
	public void onSensorActivated(Sensor s);
	public void onSensorDeactivated(String sensorId);
	
	public void onNewSensorData(String sensorId, double value);

}
