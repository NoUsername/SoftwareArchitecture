package at.fhooe.mcm441.server.clients;

import java.util.Random;

import org.slf4j.Logger;

import at.fhooe.mcm441.commons.network.Client;
import at.fhooe.mcm441.commons.network.NetworkServiceClient;
import at.fhooe.mcm441.commons.util.Definitions;
import at.fhooe.mcm441.sensor.Sensor;
import at.fhooe.mcm441.server.Server;
import at.fhooe.mcm441.server.preferences.Preferences;

/**
 * Unit test for simple App.
 */
public class FakeTestServer extends ClientAbstractionPooled
{
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this
			.getClass().getName());
	
	
	public static void main(String[] args) {
		FakeTestServer server = new FakeTestServer();
		server.startClientAbstraction();
	}
	
	public FakeTestServer() {
		new Thread(new Runnable() {	
			@Override
			public void run() {
				Random r = new Random();
				while (true) {
					sendRandomSensorData(r, false);
				}
			}
		}).start();
		
		new Thread(new Runnable() {	
			@Override
			public void run() {
				Random r = new Random();
				while (true) {
					sendRandomSensorData(r, true);
				}
			}
		}).start();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNewClient(Client data, NetworkServiceClient connection) {
		log.info("a new client has connected, we send it some fake sensor info");
		super.onNewClient(data, connection);
		
		String s1 = Definitions.PREFIX_SENSORS_VISIBILITY + ".sensor1";
		String s2 = Definitions.PREFIX_SENSORS_VISIBILITY + ".sensor2";
		String s3 = Definitions.PREFIX_SENSORS_VISIBILITY + ".sensor5";
		
		Preferences p = Server.getPreferences();
		if (p.getValue(s1) == null) {
			log.info("adding preferences");
			p.addNewPreference(s1, "true");
			p.addNewPreference(s2, "true");
			p.addNewPreference(s3, "true");
		} else {
			log.info("updating preferences");
			p.updatePreference(s1, "true");
			p.updatePreference(s2, "true");
			p.updatePreference(s3, "true");
		}
		
	}
	
	@Override
	public void onClientDisconnectes(Client c) {
		int ccount = m_clients.getAllClients().size();
		if (ccount % 5 == 0)
			log.info("currently got " + ccount + " clients");
		// why does the log only work when put before the super. call?!
		super.onClientDisconnectes(c);
	}
	
	public void sendRandomSensorData(Random r, boolean beReliable) {
		String[] sensors = new String[]{"sensor1", "sensor-x", "sensor2", "sensor3", "sensor4", "sensor5"};
		Sensor s = null;
		for (int i=0; i<sensors.length; i++) {
			String name = sensors[i];
			if (!beReliable) {
				name = "bad" + name;
			}
			s = new Sensor(name, "i am some sensor", false, r.nextDouble(), "units");
			if (!beReliable) {
				// unreliable sensors add some lag or don't send at all
				if (r.nextInt(5) == 1) {
					try {
						Thread.sleep(r.nextInt(500));
					} catch (InterruptedException e) {
					}
					super.onSensorDataReceived(s);
				}
			} else {
				super.onSensorDataReceived(s);
			}
		}
		if (beReliable) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
