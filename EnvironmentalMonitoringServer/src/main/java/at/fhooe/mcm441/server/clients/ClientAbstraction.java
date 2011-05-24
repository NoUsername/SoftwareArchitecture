package at.fhooe.mcm441.server.clients;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import at.fhooe.mcm441.commons.Configuration;
import at.fhooe.mcm441.commons.Configuration.SettingType;
import at.fhooe.mcm441.commons.network.Client;
import at.fhooe.mcm441.commons.network.IMultiClientNetworkEventsListener;
import at.fhooe.mcm441.commons.network.NetworkServiceClient;
import at.fhooe.mcm441.commons.protocol.AdminServerProtocolAbstractor;
import at.fhooe.mcm441.commons.protocol.IServerCommandListener;
import at.fhooe.mcm441.commons.protocol.ServerProtocolAbstractor;
import at.fhooe.mcm441.commons.util.Definitions;
import at.fhooe.mcm441.sensor.Sensor;
import at.fhooe.mcm441.server.Server;
import at.fhooe.mcm441.server.preferences.IChangeListener;
import at.fhooe.mcm441.server.preferences.Preferences;
import at.fhooe.mcm441.server.processing.ISensorDataListener;

public class ClientAbstraction implements IChangeListener,
		IServerCommandListener, IMultiClientNetworkEventsListener,ISensorDataListener {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this
			.getClass().getName());

	private boolean m_isStarted = false;

	protected ClientManager m_clients = null;

	protected Map<Client, ServerClient> m_adminClients = null;

	private Preferences m_prefs;

	/** preferences-prefix for polling time, the sensor-id will be appended */
	private static final String POLLTIME = Definitions.PREFIX_SENSORS_POLLTIME
			+ Definitions.PREFIX_SEPERATOR;
	
	private static final String VISIBILITY = Definitions.PREFIX_SENSORS_VISIBILITY + Definitions.PREFIX_SEPERATOR;
	
	public ClientAbstraction() {
		// this callback for the ClientManager only triggers when there is a
		// real change in the client-list
		// it's used to inform the admin clients about all the current clients
		m_clients = new ClientManager(cleanClientListener);
		m_prefs = Server.getPreferences();
		m_adminClients = new HashMap<Client, ServerClient>();

		// register at preferences for sensor visibility changes
		Server.getPreferences().register(Definitions.PREFIX_SENSORS_VISIBILITY,
				this);
	}

	public void startClientAbstraction() {
		if (m_isStarted) {
			log.warn("tried to start client abstraction component a second time!");
		} else {
			new ServerNetworkConnection(this);
			m_isStarted = true;
		}
	}

	@Override
	public void onSensorDataReceived(Sensor s) {
		ArrayList<ServerClient> targets = null;
		synchronized (m_clients) {
			// copy the entries to be reistent to modifications while sending
			List<ServerClient> clients = m_clients.getClientsForSensor(s.ident);
			if (clients != null) {
				targets = new ArrayList<ServerClient>(clients);
			}
		}

		if (targets != null && targets.size() > 0) {
			log.trace("broadcasting new sensor data from " + s.ident + " to "
					+ targets.size() + " clients");
			String msgToSend = ServerProtocolAbstractor
					.createSensorDataMessage(s.ident, s.data);
			sendToClients(targets, msgToSend);
		} else {
			log.trace("broadcasting new sensor data from " + s.ident
					+ " to 0 clients");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNewClient(Client data, NetworkServiceClient connection) {
		ServerClient sc = new ServerClient(data, connection);
		m_clients.addClient(sc);
				
		List<Sensor> allSensors = Server.getSensorStorage().getAllSensors();
		
		for (Sensor sensor : allSensors) {
			String value = m_prefs.getValue(VISIBILITY + sensor.ident);
			if ("true".equals(value)) {
				String msg = ServerProtocolAbstractor.createSensorVisibilityMessage(sensor.ident, sensor.description, sensor.dataType, true);
				sendToClient(sc, msg);
			}
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onClientDisconnectes(Client data) {
		ServerClient sc = m_clients.getServerClientByClientInfo(data);
		if (sc != null) {
			synchronized (m_clients) {
				m_clients.removeClient(sc);
			}
			sc.tryClose();
		} else {
			// log.warn("cannot find client " + data.m_id);
		}
	}

	/**
	 * triggered when an admin client connects
	 * 
	 * @param data
	 * @param connection
	 */
	public void onNewAdminClient(Client data, NetworkServiceClient connection) {
		// also trigger the normal client handling
		onNewClient(data, connection);

		ServerClient newAdminClient = new ServerClient(data, connection);
		synchronized (m_adminClients) {
			m_adminClients.put(data, newAdminClient);
		}
		
		// tell him about all the server configs + send him list of all clients
		List<ServerClient> all = null;
		synchronized (m_clients) {
			// copy the entries to be reistent to modifications while sending
			List<ServerClient> targets = m_clients.getAllClients();
			all = new ArrayList<ServerClient>(targets);
		}

		String msg = "";
		// tell him about all the clients that are connected
		for (ServerClient sc : all) {
			msg = AdminServerProtocolAbstractor
					.createClientMessage(sc.getClientInfo().m_id,
							sc.getClientInfo().m_address, true);
			sendToClient(newAdminClient, msg);
		}

		// tell him about all server configs:
		// TODO maybe add more configs!
		String pollTimeKey = Definitions.PREFIX_SERVER_DEFAULT_POLLING;
		String pollTimeValue = m_prefs.getValue(pollTimeKey);
		msg = AdminServerProtocolAbstractor
				.createConfigMessage(new Configuration(
						"default polling time: ", pollTimeKey,
						SettingType.number, pollTimeValue));
		sendToClient(newAdminClient, msg);

		
		// send all invisible sensors as sensor-config items
		// + send all polling sensors as polling frequency objects
		List<Sensor> allSensors = Server.getSensorStorage().getAllSensors();
		for (Sensor sensor : allSensors) {
			msg = createSensorVisiblityConf(sensor);
			sendToClient(newAdminClient, msg);
			if (sensor.isPolling) {
				msg = createSensorPollTimeConf(sensor);
				sendToClient(newAdminClient, msg);
			}
		}
		
	}

	/**
	 * this creates a config item for the admin clients which allows them
	 * to toggle the visibility of a sensor
	 * @param sensor the sensor
	 */
	private String createSensorVisiblityConf(Sensor sensor) {
		String sensorsVisibilityKey = VISIBILITY + sensor.ident;
		String value = m_prefs.getValue(sensorsVisibilityKey);
		Configuration config = new Configuration("visibility of sensor " + sensor.description, sensorsVisibilityKey, SettingType.bool, value);
		return AdminServerProtocolAbstractor.createSensorConfigMessage(sensor.ident, config);
	}
	
	/**
	 * creates a config item for the sensors polling time
	 * @param sensor
	 * @return
	 */
	private String createSensorPollTimeConf(Sensor sensor) {
		String sensorsPollTimeKey = POLLTIME + sensor.ident;
		String value = m_prefs.getValue(sensorsPollTimeKey);
		Configuration config = new Configuration("polling time of sensor " + sensor.description, sensorsPollTimeKey, SettingType.number, value);
		return AdminServerProtocolAbstractor.createSensorConfigMessage(sensor.ident, config);
	}
	
	/**
	 * when an admin client disconnects, it is removed from the admin client list
	 * @param data
	 */
	public void onAdminClientDisconnects(Client data) {
		onClientDisconnectes(data);
		synchronized (m_adminClients) {
			m_adminClients.remove(data);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(String key, String msg) {
		if (key.startsWith(Definitions.PREFIX_SENSORS_VISIBILITY)) {
			// notify all clients
			log.trace("a sensor has become (in)visible");
			String sensorId = key.replace(VISIBILITY, "");
			String msgToSend = null;
			if ("true".equals(msg)) {
				// for a visible message, we need all the sensors information
				Sensor s = Server.getSensorStorage().getSensorById(sensorId);
				if (s != null) {
					msgToSend = ServerProtocolAbstractor.createSensorVisibilityMessage(sensorId, s.description, s.dataType, true);
					broadcastToAllClients(msgToSend);
					// create the visibility conf item for the admin clients:
					String confMsg = createSensorVisiblityConf(s);
					List<ServerClient> admins = null;
					synchronized (m_adminClients) {
						// copy to be thread-safe
						admins = new ArrayList<ServerClient>(m_adminClients.values());
					}
					sendToClients(admins, confMsg);
					
					// if its a polling sensor, we additionally need the polltime config:
					if (s.isPolling) {
						confMsg = createSensorPollTimeConf(s);
						sendToClients(admins, confMsg);
					}
				} else {
					log.warn("we got info about new sensor which isn't really there! id=" + sensorId);
				}
			} else {
				// sensor became invisble, we only need his id
				msgToSend = ServerProtocolAbstractor.createSensorVisibilityMessage(sensorId, "NOT_USED", "NOT_USED", false);
				broadcastToAllClients(msgToSend);
				// and unsubscribe all clients that might currently be subscribed:
				List<ServerClient> targets = null;
				synchronized (m_clients) {						
					List<ServerClient> clients = m_clients.getClientsForSensor(sensorId);
					if (clients != null) {
						targets = new ArrayList<ServerClient>(clients);
					}
				}
				if (targets != null) {
					for (ServerClient unregMe : targets) {
						m_clients.onClientUnregistersForSensor(unregMe, sensorId);
					}
				}
			}
		}
	}

	protected void broadcastToAllClients(String msg) {
		// TODO: implement broadcasting, consider the following:
		// don't block the caller, because this could take some time
		// are clients where we lose the connection already handled?

		// XXX this dummy implementation BLOCKS THE WHOLE TIME!!!
		List<ServerClient> all = null;
		synchronized (m_clients) {
			// copy the entries to be reistent to modifications while sending
			List<ServerClient> targets = m_clients.getAllClients();
			all = new ArrayList<ServerClient>(targets);
		}

		if (log.isTraceEnabled()) {
			log.trace("sending to all " + all.size() + "clients");
		}
		for (ServerClient sc : all) {
			sendToClient(sc, msg);
		}
	}

	protected void sendToClients(Collection<ServerClient> clients, String msg) {
		// TODO: implement broadcasting, consider the following:
		// don't block the caller, because this could take some time
		// are clients where we lose the connection already handled?

		// XXX this dummy implementation BLOCKS THE WHOLE TIME!!!
		for (ServerClient sc : clients) {
			sendToClient(sc, msg);
		}
	}

	/**
	 * this method sends to a client, if you want to make the sending parallel,
	 * overwrite this method and implement thread-pooling there
	 * 
	 * @param client
	 * @param msg
	 */
	protected void sendToClient(ServerClient client, String msg) {
		client.getClientConnection().sendMessage(msg);
	}

	/**
	 * this is ONLY triggered when a client really connects/disconnects, so
	 * duplicate connect/disconnect messags are filtered
	 * 
	 * when this is triggered, we inform the admin clients
	 */
	IClientRegistrationListener cleanClientListener = new IClientRegistrationListener() {
		@Override
		public void onClientRegistrationChanged(Client c, boolean registered) {
			List<Client> admins = null;
			// copy to avoid threading problems
			synchronized (m_adminClients) {
				admins = new ArrayList<Client>(m_adminClients.keySet());
			}
			String msg = AdminServerProtocolAbstractor.createClientMessage(
					c.m_id, c.m_address, registered);
			// send the client-change to every admin client
			for (Client admin : admins) {
				sendToClient(m_clients.getServerClientByClientInfo(admin), msg);
			}
		}
	};
	/**
	 * get the number of connected clients, for status tracking
	 * @return
	 */
	public int getClientCount() {
		return m_clients.getAllClients().size();
	}
	
	/*******
	 * messages that the server will be notified about
	 ******/

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onRegisterForSensor(Client c, String sensorId, boolean status) {
		ServerClient sc = m_clients.getServerClientByClientInfo(c);
		if (sc == null) {
			log.warn("cannot find client! " + c.m_id);
			return;
		}

		if (status) {
			m_clients.onClientRegisteresForSensor(sc, sensorId);
		} else {
			m_clients.onClientUnregistersForSensor(sc, sensorId);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onAdminConfigCommand(Client c, String conf, String data) {
		log.info("admin client set configuration: " + conf + " to: " + data);
		m_prefs.updatePreference(conf, data);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onAdminSensorConfigCommand(Client c, String sensorId,
			String conf, String data) {
		log.info("admin client set sensor configuration: " + conf
				+ " for sensor: " + sensorId + " to: " + data);
		// we generate the confIds in a way that they are dedicated to one
		// sensor anyway, so we don't really need the sensorId here
		m_prefs.updatePreference(conf, data);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onByeMessage(Client c) {
		onClientDisconnectes(c);
	}

}
