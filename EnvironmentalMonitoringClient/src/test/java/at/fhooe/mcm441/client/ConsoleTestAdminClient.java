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
import at.fhooe.mcm441.commons.util.Util;
import at.fhooe.mcm441.sensor.Sensor;

public class ConsoleTestAdminClient implements IAdminClientSideListener,
		IConnectionStatusListener {
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(ConsoleTestAdminClient.class.getName());

	public static String HOST = "localhost";
	public static int PORT = 4445;
	
	private static final boolean HARDCORETEST = true; // if this is true, not one but MANY clients are started
	private static final boolean LOGGING = !HARDCORETEST;
	
	private static final int MIN_STAY_CONNECTED_TIME = 120; // seconds
	private static final int STARTED_CLIENTS_COUNT = 100;
	private static final int MIN_STARTING_OFFSET = 100; // milliseconds

	public AdminConnection m_con;
	private boolean m_autoRegister = false;
	private ArrayList<String> m_sensors = new ArrayList<String>();

	private Boolean connected = null;
	private static int clientsConnectedCount = 0;
	private static long msgsReceivedCount = 0;

	public static void main(String[] args) throws Exception {
		if (!HARDCORETEST) {
			new ConsoleTestAdminClient(true, 0);
		} else {
			hardCoreTest();
		}
	}
	
	public static void hardCoreTest() throws Exception {
		long started = System.currentTimeMillis();
		final Random r = new Random();
		for (int i=0; i<STARTED_CLIENTS_COUNT; i++) {
			new Thread(new Runnable() {
				public void run() {
					try {
						new ConsoleTestAdminClient(true, MIN_STAY_CONNECTED_TIME + r.nextInt(8));
					} catch (Exception e) {
						e.printStackTrace();
					}
			}}).start();
			Thread.sleep(MIN_STARTING_OFFSET+r.nextInt(10));
			if (i%10 == 0)
				System.err.println(" WE ARE AT " + i + " of " + STARTED_CLIENTS_COUNT + " clients");
		}
		
		// log status:
		for (int i=0; i<10; i++) {
			Util.sleep(5000);
			log.info("CLIENTS CONNECTED INFO: " + clientsConnectedCount);
			log.info("TOTAL MSGS RECEIVED: " + msgsReceivedCount);
			log.info("IN : " + (System.currentTimeMillis() - started) + " ms");
		}
	}

	public ConsoleTestAdminClient(boolean autoRegister, int disconnectAfterSeconds) throws Exception {
		m_autoRegister = autoRegister;
		m_con = new AdminConnection(HOST, PORT, this, this);

		// do some funky busy waiting:
		while (connected == null) {
			Thread.sleep(20);
		}
		
		if (disconnectAfterSeconds > 0) {
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					if (LOGGING)
						log.info("AUTOMATICALLY DISCONNECTING");
					m_con.close();
				}
			}, 1000 * disconnectAfterSeconds);
		}
		
	}

	@Override
	public void onSensorActivated(Sensor s) {
		if (LOGGING)
			log.info("sensor activated: " + s);
		if (m_autoRegister) {
			if (LOGGING)
				log.info("automatically registering for sensor!");
			if (!m_sensors.contains(s.ident)) {
				m_con.registerForSensor(s.ident, true);
				m_sensors.add(s.ident);
			} else {
				//log.info("already registered for that sensor");
			}
		}
		msgsReceivedCount++;
	}

	@Override
	public void onSensorDeactivated(String sensorId) {
		if (LOGGING)
			log.info("sensor " + sensorId + " deactivated");
		msgsReceivedCount++;
	}

	@Override
	public void onNewSensorData(String sensorId, double value) {
		if (LOGGING)
			log.info("sensordata " + sensorId + " " + value);
		msgsReceivedCount++;
	}

	@Override
	public void onConnectionEstablished() {
		if (LOGGING)
			log.info("we are connected");
		connected = Boolean.TRUE;
		clientsConnectedCount++;
	}

	@Override
	public void onConnectionLost() {
		if (LOGGING)
			log.info("we got disconnected");
		connected = Boolean.FALSE;
	}

	@Override
	public void onSensorConfigurationItem(String sensorId, Configuration conf) {
		if (LOGGING)
			log.info("sensor conf item for sensor " + sensorId + " " + conf);
		msgsReceivedCount++;
	}

	@Override
	public void onServerConfigurationItem(Configuration conf) {
		if (LOGGING)
			log.info("server conf item " + conf);
		msgsReceivedCount++;
	}

	@Override
	public void onClientConnected(Client client) {
		if (LOGGING)
			log.info("client connected " + client);
		msgsReceivedCount++;
	}

	@Override
	public void onClientDisconnected(Client client) {
		if (LOGGING)
			log.info("client disconnected " + client);
		msgsReceivedCount++;
	}

}
