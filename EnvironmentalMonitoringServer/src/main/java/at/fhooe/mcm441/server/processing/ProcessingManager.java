package at.fhooe.mcm441.server.processing;

import java.util.ArrayList;
import java.util.List;

import at.fhooe.mcm441.sensor.Sensor;

/**
 * process the data and notify the listener objects about new data
 * 
 * @author Manuel Lachberger
 * 
 */
public class ProcessingManager implements ISensorListenerService,
		ISensorDataListener {

	/**
	 * all the sensor data listener
	 */
	List<ISensorDataListener> m_sensorDataListeners = null;

	/**
	 * default constructor
	 */
	public ProcessingManager() {
		m_sensorDataListeners = new ArrayList<ISensorDataListener>();
	}

	/**
	 * do sthg with the data (depends on the sensor what to do)
	 * 
	 * @param sensor
	 *            the sensor with the data
	 * @return the new sensor object with the processed data
	 */
	public Sensor processData(Sensor sensor) {
		if (sensor != null) {
			// TODO: process the data
			// ...
			return sensor;
		}
		return null;
	}

	@Override
	public void onSensorDataReceived(Sensor sensor) {
		sensor = processData(sensor);
		if (m_sensorDataListeners != null) {
			for (ISensorDataListener listener : m_sensorDataListeners) {
				if (listener != null) {
					listener.onSensorDataReceived(sensor);
				}
			}
		}
	}

	@Override
	public void register(ISensorDataListener listener) {
		if (m_sensorDataListeners != null) {
			m_sensorDataListeners.add(listener);
		}
	}

	@Override
	public void unregister(ISensorDataListener listener) {
		if (m_sensorDataListeners != null) {
			m_sensorDataListeners.remove(listener);
		}
	}
}
