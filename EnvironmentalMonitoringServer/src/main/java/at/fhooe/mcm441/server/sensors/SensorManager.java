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
import at.fhooe.mcm441.commons.util.Definitions;
import at.fhooe.mcm441.commons.util.IPoolObserver;
import at.fhooe.mcm441.commons.util.PooledExecutor;
import at.fhooe.mcm441.commons.util.Util;
import at.fhooe.mcm441.sensor.Sensor;
import at.fhooe.mcm441.server.Server;
import at.fhooe.mcm441.server.preferences.Preferences;

/**
 * Abstracts the sensor connection
 * 
 * Opens a serversocket to which sensors can connect to
 * 
 * when new sensors are connected and have sent their information, the new
 * sensor will be announced
 * 
 * this class also takes care of polling and takes into account the current
 * polling times set in the preferences
 * 
 * @author Paul Klingelhuber
 */
public class SensorManager implements IMultiClientNetworkListener, IMultiClientNetworkEventsListener,
		ISensorProtocolListener, IPoolObserver, ISensorStorage {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());

	/** preferences-prefix for polling time, the sensor-id will be appended */
	private static final String POLLTIME = Definitions.PREFIX_SENSORS_POLLTIME
			+ Definitions.PREFIX_SEPERATOR;
	private static final String VISIBILITY = Definitions.PREFIX_SENSORS_VISIBILITY
			+ Definitions.PREFIX_SEPERATOR;
	/** all sensors that have to be polled */
	private List<ServerSensor> m_pollSensors = new ArrayList<ServerSensor>();
	/** to get sensor objects by their id */
	private Map<String, ServerSensor> m_sensors = new Hashtable<String, ServerSensor>();
	/** protocol, for processing and creating appropriate messages */
	private SensorProtocol m_protocol = new SensorProtocol(this);
	/** preferences class, so that we don't have to retrieve it everytime */
	private Preferences m_prefs = null;
	/** as long as this is true, the polling will take place */
	private boolean m_running = true;
	/** our threadpool for polling */
	private PooledExecutor m_executor;

	/**
	 * constructor, will also already start everything
	 */
	public SensorManager() {
		MultiClientNetworkService server = new MultiClientNetworkService(this,
				this);
		String sPort = Server.getPreferences().getValue(
				Definitions.PREFIX_SERVER_SENSORS_PORT);
		int port = Integer.parseInt(sPort);
		server.startListening(port);
		m_prefs = Server.getPreferences();
		m_executor = new PooledExecutor(25, 100, 1000, this);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				startPollingThread();
			}
		});
		t.setName("polling thread");
		t.start();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Sensor> getAllSensors() {
		List<Sensor> all = new ArrayList<Sensor>();
		for (ServerSensor sens : m_sensors.values()) {
			all.add(sens.sensor);
		}
		return all;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Sensor getSensorById(String sensorId) {
		ServerSensor ssens = m_sensors.get(sensorId);
		return (ssens != null) ? ssens.sensor : null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNewClient(Client c, NetworkServiceClient sc) {
		Sensor s = new Sensor(c.m_id, "", false, 0.0, "");

		m_sensors.put(c.m_id, new ServerSensor(s, sc));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onClientDisconnectes(Client c) {
		ServerSensor container = null;
		synchronized (m_sensors) {
			container = m_sensors.get(c.m_id);
		}
		if (container != null) {
			synchronized (m_pollSensors) {
				m_pollSensors.remove(container);
			}
			synchronized (m_sensors) {
				m_sensors.remove(c.m_id);
			}
			log.info("sensor " + c.m_id + " no longer available");

			m_prefs.updatePreference(VISIBILITY + c.m_id, "false");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNewPackage(Client from, String newPackage) {
		m_protocol.parseMessage(from, newPackage);
	}

	@Override
	public void onSensorInfo(Client c, String description, String dataType,
			boolean isPolling) {
		ServerSensor container = m_sensors.get(c.m_id);
		container.sensor.description = description;
		container.sensor.dataType = dataType;
		container.sensor.isPolling = isPolling;
		if (isPolling) {
			synchronized (m_pollSensors) {
				m_pollSensors.add(container);
			}
			String defaultPolling = m_prefs
					.getValue(Definitions.PREFIX_SERVER_DEFAULT_POLLING);
			int time = Integer.parseInt(defaultPolling);
			container.nextPolling = System.currentTimeMillis() + time;
			m_prefs.addNewPreference(POLLTIME + c.m_id, defaultPolling);
		}

		if (log.isTraceEnabled()) {
			log.trace("got new sensor: " + container.sensor);
		}

		m_prefs.addNewPreference(VISIBILITY + c.m_id, "true");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onSensorData(Client c, double data) {
		ServerSensor container = m_sensors.get(c.m_id);
		Sensor sensor = container.sensor;
		sensor.data = data;

		log.trace("got data from sensor: " + c.m_id + " " + data);

		// notify processing unit
		Server.getProcessingManager().onSensorDataReceived(sensor);
	}

	private void startPollingThread() {
		while (m_running) {
			long now = System.currentTimeMillis();
			synchronized (m_pollSensors) {
				for (final ServerSensor s : m_pollSensors) {
					if (s.nextPolling < now) {
						m_executor.execute(new Runnable() {
							@Override
							public void run() {
								s.con.sendMessage("gimmedata");
							}
						}, -1, null);

						String defaultPolling = m_prefs.getValue(POLLTIME
								+ s.sensor.ident);
						int time = Integer.parseInt(defaultPolling);
						s.nextPolling = now + time;
					}
				}
			}
			Util.sleep(10);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onQueueLimitReached(int currentSize) {
		log.warn("thread pool queue overfull: " + currentSize);
	}

	/**
	 * internal data-container for storing sensors and the network connections
	 */
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
}
