package at.fhooe.mcm441.server.sensors;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import at.fhooe.mcm441.commons.network.Client;
import at.fhooe.mcm441.commons.network.IMultiClientNetworkEventsListener;
import at.fhooe.mcm441.commons.network.IMultiClientNetworkListener;
import at.fhooe.mcm441.commons.network.MultiClientNetworkService;
import at.fhooe.mcm441.commons.network.NetworkServiceClient;
import at.fhooe.mcm441.commons.protocol.ISensorProtocolListener;
import at.fhooe.mcm441.commons.protocol.SensorProtocol;
import at.fhooe.mcm441.commons.util.IPoolObserver;
import at.fhooe.mcm441.commons.util.PooledExecutor;
import at.fhooe.mcm441.commons.util.Util;
import at.fhooe.mcm441.sensor.Sensor;
import at.fhooe.mcm441.server.Server;
import at.fhooe.mcm441.server.preferences.Preferences;
import at.fhooe.mcm441.server.utility.Definitions;


public class SensorManager implements IMultiClientNetworkListener, IMultiClientNetworkEventsListener, ISensorProtocolListener, IPoolObserver {
	class ServerSensor {
		public ServerSensor(Sensor sensor, NetworkServiceClient con) {
			super();
			this.sensor = sensor;
			this.con = con;
		}
		public Sensor sensor;
		public NetworkServiceClient con;
		public long nextPolling = 0;
	}
	
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
	
	List<ServerSensor> pollSensors = new ArrayList<ServerSensor>();
	
	Map<String, ServerSensor> sensors = new Hashtable<String, ServerSensor>();
	
	private SensorProtocol protocol = new SensorProtocol(this);
	
	private Preferences prefs = null;
	
	private static final String POLLTIME = Definitions.PREFIX_SENSORS_POLLTIME + Definitions.PREFIX_SEPERATOR;
	
	private boolean m_running = true;
	
	private PooledExecutor executor;
	
	
	public SensorManager() {
		MultiClientNetworkService server = new MultiClientNetworkService(this, this);
		String sPort = Server.getPreferences().getValue(Definitions.PREFIX_SERVER_SENSORS_PORT);
		int port = Integer.parseInt(sPort);
		server.startListening(port);
		prefs = Server.getPreferences();
		executor = new PooledExecutor(25, 100, 1000, this);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				startPollingThread();
			}
		});
		t.setName("polling thread");
		t.start();
	}

	@Override
	public void onNewClient(Client c, NetworkServiceClient sc) {
		Sensor s = new Sensor(c.m_id, "", false, 0.0, "");
		
		sensors.put(c.m_id, new ServerSensor(s, sc));
	}

	@Override
	public void onClientDisconnectes(Client c) {
		ServerSensor container = null;
		synchronized (sensors) {
			container = sensors.get(c.m_id);			
		}
		if (container != null) {
			synchronized (pollSensors) {
				pollSensors.remove(container);
			}
			synchronized (sensors) {
				sensors.remove(c.m_id);
			}
			log.info("sensor " + c.m_id + " no longer available");
			// TODO inform everybody
		}
	}

	@Override
	public void onNewPackage(Client from, String newPackage) { 
		protocol.parseMessage(from, newPackage);
	}

	@Override
	public void onSensorInfo(Client c, String description, String dataType, boolean isPolling) {
		ServerSensor container = sensors.get(c.m_id);
		container.sensor.description = description;
		container.sensor.dataType = dataType;
		container.sensor.isPolling = isPolling;
		if (isPolling) {
			pollSensors.add(container);
			String defaultPolling = prefs.getValue(Definitions.PREFIX_SERVER_DEFAULT_POLLING);
			int time = Integer.parseInt(defaultPolling);
			container.nextPolling = System.currentTimeMillis() + time;
			prefs.addNewPreference(POLLTIME + c.m_id, defaultPolling);
		}
		
		log.info("got new sensor: " + container.sensor);
		// TODO tell sb about new sensor
		
	}

	@Override
	public void onSensorData(Client c, double data) {
		ServerSensor container = sensors.get(c.m_id);
		Sensor sensor = container.sensor;
		sensor.data = data;
		
		log.info("got data from sensor: " + c.m_id + " " + data);
		
		// TODO notify processing unit
		
	}
	
	private void startPollingThread() {
		while (m_running) {
			long now = System.currentTimeMillis();
			synchronized (pollSensors) {
			for (final ServerSensor s : pollSensors) {
				if (s.nextPolling < now) {
					executor.execute(new Runnable() {
						@Override
						public void run() {
							s.con.sendMessage("gimmedata");
						}
					}, -1, null);
					
					String defaultPolling = prefs.getValue(POLLTIME + s.sensor.ident);
					int time = Integer.parseInt(defaultPolling);
					s.nextPolling = now + time;
				}
			}
			}
			Util.sleep(10);
		}
	}

	@Override
	public void onQueueLimitReached(int currentSize) {
		log.warn("thread pool queue overfull: " + currentSize);
	}
	
}
