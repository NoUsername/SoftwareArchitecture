package at.fhooe.mcm441.server.processing;

import at.fhooe.mcm441.sensor.Sensor;

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
