package at.fhooe.mcm441.commons.protocol;

import at.fhooe.mcm441.commons.network.Client;

/**
 * Interface for callbacks that come from successfully parsed sensor-messages
 * 
 * @author Paul Klingelhuber
 */
public interface ISensorProtocolListener {
	/**
	 * the sensor sent us his information
	 * 
	 * @param c the client who sent it
	 * @param description textual description
	 * @param dataType the datatype, like "°C"
	 * @param isPolling when true, we have to poll the sensor
	 */
	void onSensorInfo(Client c, String description, String dataType, boolean isPolling);
	
	/**
	 * the sensor sent us new data 
	 * @param c sensor who sent it
	 * @param data new data value that was sent
	 */
	void onSensorData(Client c, double data);
}
