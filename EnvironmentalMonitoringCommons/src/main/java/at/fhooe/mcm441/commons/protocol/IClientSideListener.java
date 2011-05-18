package at.fhooe.mcm441.commons.protocol;

import at.fhooe.mcm441.sensor.Sensor;

/**
 * stuff that the server invokes on the client
 * 
 * @author Paul Klingelhuber
 */
public interface IClientSideListener {
	/**
	 * a new sensor got activated
	 * @param s
	 */
	public void onSensorActivated(Sensor s);
	
	/**
	 * sensor with that id has got deactivated
	 * @param sensorId
	 */
	public void onSensorDeactivated(String sensorId);
	
	/**
	 * there is new data for a sensor
	 * @param sensorId
	 * @param value
	 */
	public void onNewSensorData(String sensorId, double value);

}
