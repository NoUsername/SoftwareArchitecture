package at.fhooe.mcm441.commons.protocol;

import at.fhooe.mcm441.commons.network.Client;

public interface ISensorProtocolListener {
	void onSensorInfo(Client c, String description, String dataType, boolean isPolling);
	void onSensorData(Client c, double data);
}
