package at.fhooe.mcm441.client;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;

import at.fhooe.mcm441.commons.Configuration;
import at.fhooe.mcm441.commons.network.Client;
import at.fhooe.mcm441.commons.network.IConnectionStatusListener;
import at.fhooe.mcm441.commons.protocol.IAdminClientSideListener;
import at.fhooe.mcm441.sensor.Sensor;

public class ConsoleTestAdminClient implements IAdminClientSideListener,
		IConnectionStatusListener {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this
			.getClass().getName());

	public static String HOST = "localhost";
	public static int PORT = 4444;

	public AdminConnection m_con;
	private boolean m_autoRegister = false;
	private ArrayList<String> m_sensors = new ArrayList<String>();

	private Boolean connected = null;

	public static void main(String[] args) throws Exception {
		if (true) {
			new ConsoleTestAdminClient(true, 0);
		} else {
			hardCoreTest();
		}
	}
	
	public static void hardCoreTest() throws Exception {
		final Random r = new Random();
		for (int i=0; i<200; i++) {
			new Thread(new Runnable() {
				public void run() {
					try {
						new ConsoleTestAdminClient(true, 10 + r.nextInt(8));
					} catch (Exception e) {
						e.printStackTrace();
					}
			}}).start();
			Thread.sleep(r.nextInt(10));
			if (i%10 == 0)
				System.err.println(" WE ARE AT " + i + " of " + 50 + " clients");
		}
	}

	public ConsoleTestAdminClient(boolean autoRegister, int disconnectAfterSeconds) throws Exception {
		m_autoRegister = autoRegister;
		m_con = new AdminConnection(HOST, PORT, this, this);

		// do some funky busy waiting:
		while (connected == null) {
			Thread.sleep(20);
		}

		m_con.registerForSensor("sensor-x", true);
		Thread.sleep(1000);
		m_con.registerForSensor("sensor-x", false);
		Thread.sleep(1000);
		m_con.setSensorConfig("sensor-x", "pollingtime", "100");
		Thread.sleep(1000);
		m_con.setServerConfig("maxclients", "10");
		
		if (disconnectAfterSeconds > 0) {
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					log.info("AUTOMATICALLY DISCONNECTING");
					m_con.close();
				}
			}, 1000 * disconnectAfterSeconds);
		}
		
	}

	@Override
	public void onSensorActivated(Sensor s) {
		log.info("sensor activated: " + s);
		if (m_autoRegister) {
			log.info("automatically registering for sensor!");
			if (!m_sensors.contains(s.ident)) {
				m_con.registerForSensor(s.ident, true);
				m_sensors.add(s.ident);
			} else {
				//log.info("already registered for that sensor");
			}
		}
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
		connected = Boolean.TRUE;
	}

	@Override
	public void onConnectionLost() {
		log.info("we got disconnected");
		connected = Boolean.FALSE;
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
