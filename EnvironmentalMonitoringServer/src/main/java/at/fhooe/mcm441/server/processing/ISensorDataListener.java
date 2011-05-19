package at.fhooe.mcm441.server.processing;

import at.fhooe.mcm441.sensor.Sensor;

/**
 * the sensor data listener interface -> what do you want to do when new sensor
 * data is received
 * 
 * @author Manuel Lachberger
 * 
 */
public interface ISensorDataListener {
	/**
	 * new sensor data is received
	 * 
	 * @param sensor
	 *            the new sensor data
	 */
	public void onSensorDataReceived(Sensor sensor);
}
