package at.fhooe.mcm441.client;

import org.slf4j.Logger;

import at.fhooe.mcm441.commons.Configuration;
import at.fhooe.mcm441.commons.network.Client;
import at.fhooe.mcm441.commons.network.IConnectionStatusListener;
import at.fhooe.mcm441.commons.protocol.IAdminClientSideListener;
import at.fhooe.mcm441.sensor.Sensor;

public class ConsoleTestAdminClient implements IAdminClientSideListener, IConnectionStatusListener {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
	
	public static String HOST = "localhost";
	public static int PORT = 4444;
	
	public AdminConnection con;
	
	public static void main(String[] args) throws Exception {
		new ConsoleTestAdminClient();
	}
	
	public ConsoleTestAdminClient() throws Exception {
		con = new AdminConnection(HOST, PORT, this, this);
		con.registerForSensor("sensor-x", true);
		Thread.sleep(1000);
		con.registerForSensor("sensor-x", false);
		Thread.sleep(1000);
		con.setSensorConfig("sensor-x", "pollingtime", "100");
		Thread.sleep(1000);
		con.setServerConfig("maxclients", "10");
	}

	@Override
	public void onSensorActivated(Sensor s) {
		log.info("sensor activated: " + s);
	}

	@Override
	public void onSensorDeactivated(String sensorId) {
		log.info("sensor " + sensorId + " deactivated");		
	}

	@Override
	public void onNewSensorData(String sensorId, double value) {
		log.info("sensordata " + sensorId + " " + value);
	}

	@Override
	public void onConnectionEstablished() {
		log.info("we are connected");
	}

	@Override
	public void onConnectionLost() {
		log.info("we got disconnected");
	}

	@Override
	public void onSensorConfigurationItem(String sensorId, Configuration conf) {
		log.info("sensor conf item for sensor " + sensorId + " " + conf);
	}

	@Override
	public void onServerConfigurationItem(Configuration conf) {
		log.info("server conf item " + conf);
	}

	@Override
	public void onClientConnected(Client client) {
		log.info("client connected " + client);
	}

	@Override
	public void onClientDisconnected(Client client) {
		log.info("client disconnected " + client);
	}

}
