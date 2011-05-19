package at.fhooe.mcm441.server.sensors;

import java.util.List;

import at.fhooe.mcm441.sensor.Sensor;

public interface ISensorStorage {
	/**
	 * returns a list of all sensors
	 * @return
	 */
	List<Sensor> getAllSensors();
	
	/**
	 * get a sensor by its id
	 * @param sensorId
	 * @return
	 */
	Sensor getSensorById(String sensorId);
}
