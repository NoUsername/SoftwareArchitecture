package at.fhooe.mcm441.Monitoring.processing;

import at.fhooe.mcm441.Monitoring.sensor.Sensor;

/**
 * process the data
 * @author manuel
 *
 */
public interface IProcessor {

	/**
	 * do something... ANYTHING
	 * @param sensor
	 */
	public void dataReceived(Sensor sensor);
}
