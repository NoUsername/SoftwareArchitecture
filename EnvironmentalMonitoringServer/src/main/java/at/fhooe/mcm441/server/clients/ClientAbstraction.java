package at.fhooe.mcm441.server.clients;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import at.fhooe.mcm441.commons.Configuration;
import at.fhooe.mcm441.commons.Configuration.SettingType;
import at.fhooe.mcm441.commons.network.Client;
import at.fhooe.mcm441.commons.network.IMultiClientNetworkEventsListener;
import at.fhooe.mcm441.commons.network.NetworkServiceClient;
import at.fhooe.mcm441.commons.protocol.AdminServerProtocolAbstractor;
import at.fhooe.mcm441.commons.protocol.IServerCommandListener;
import at.fhooe.mcm441.commons.protocol.ServerProtocolAbstractor;
import at.fhooe.mcm441.sensor.Sensor;
import at.fhooe.mcm441.server.Server;
import at.fhooe.mcm441.server.preferences.IChangeListener;
import at.fhooe.mcm441.server.preferences.Preferences;
import at.fhooe.mcm441.server.utility.Definitions;

public class ClientAbstraction implements IChangeListener, IServerCommandListener, IMultiClientNetworkEventsListener {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
	
	private boolean m_isStarted = false;
	
	protected ClientManager m_clients = null;
	
	protected List<Client> m_adminClients = null;
	
	private Preferences m_prefs;
	
	public ClientAbstraction() {
		// this callback for the ClientManager only triggers when there is a real change in the client-list
		// it's used to inform the admin clients about all the current clients
		m_clients = new ClientManager(cleanClientListener);
		m_prefs = Server.getPreferences();
		m_adminClients = new ArrayList<Client>();
		
		// register at preferences for sensor visibility changes
		Server.getPreferences().register(Definitions.PREFIX_SENSORS_VISIBILITY, this);
	}
	
	public void startClientAbstraction() {
		if (m_isStarted) {
			log.warn("tried to start client abstraction component a second time!");
		} else {
			new ServerNetworkConnection(this);
			m_isStarted = true;
		}
	}
	
	public void onNewSensorValue(Sensor s) {
		ArrayList<ServerClient> targets = null;
		synchronized (m_clients) {
			// copy the entries to be reistent to modifications while sending
			List<ServerClient> clients = m_clients.getClientsForSensor(s.ident);
			if (clients != null) {
				targets = new ArrayList<ServerClient>(clients);
			}
		}
		
		if (targets != null && targets.size() > 0) {
			log.trace("broadcasting new sensor data from " + s.ident + " to " + targets.size() + " clients");
			String msgToSend = ServerProtocolAbstractor.createSensorDataMessage(s.ident, s.data);
			sendToClients(targets, msgToSend);
		} else {
			log.trace("broadcasting new sensor data from "+ s.ident +" to 0 clients");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNewClient(Client data, NetworkServiceClient connection) {
		m_clients.addClient(new ServerClient(data, connection));
		// TODO tell him about all our sensors
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
			//log.warn("cannot find client " + data.m_id);
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
		
		// tell him about all the server configs + send him list of all clients
		
		List<ServerClient> all = null;
		synchronized (m_clients) {
			// copy the entries to be reistent to modifications while sending
			List<ServerClient>targets = m_clients.getAllClients();
			all = new ArrayList<ServerClient>(targets);
		}
		ServerClient newAdminClient = new ServerClient(data, connection);
		
		String msg = "";
		// tell him about all the clients that are connected
		for (ServerClient sc : all) {
			msg = AdminServerProtocolAbstractor.createClientMessage(sc.getClientInfo().m_id, sc.getClientInfo().m_address, true);
			sendToClient(newAdminClient, msg);
		}
		
		// tell him about all server configs:
		// TODO maybe add more configs!
		String pollTimeKey = Definitions.PREFIX_SERVER_DEFAULT_POLLING;
		String pollTimeValue = m_prefs.getValue(pollTimeKey);
		msg = AdminServerProtocolAbstractor.createConfigMessage(new Configuration("default polling time: ", pollTimeKey, SettingType.number, pollTimeValue));
		sendToClient(newAdminClient, msg);
		
		synchronized (m_adminClients) {
			m_adminClients.add(data);
		}
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
			log.info("a sensor has become (in)visible");
			String sensorId = key.replace(Definitions.PREFIX_SENSORS_VISIBILITY + ".", "");
			String msgToSend = ServerProtocolAbstractor.createSensorVisibilityMessage(sensorId, "TODO", "TODO", Boolean.parseBoolean(msg));
			broadcastToAllClients(msgToSend);
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
			List<ServerClient>targets = m_clients.getAllClients();
			all = new ArrayList<ServerClient>(targets);
		}
		
		log.info("sending to all clients " + all.size());
		for (ServerClient sc : all) {
			sendToClient(sc, msg);
		}
	}
	
	protected void sendToClients(List<ServerClient> clients, String msg) {
		// TODO: implement broadcasting, consider the following:
		// don't block the caller, because this could take some time
		// are clients where we lose the connection already handled?
		
		// XXX this dummy implementation BLOCKS THE WHOLE TIME!!!
		for (ServerClient sc : clients) {
			sendToClient(sc, msg);
		}
	}
	
	/**
	 * this method sends to a client, if you want to make the sending parallel, overwrite this method
	 * and implement thread-pooling there
	 * @param client
	 * @param msg
	 */
	protected void sendToClient(ServerClient client, String msg) {
		client.getClientConnection().sendMessage(msg);
	}
	
	/**
	 * this is ONLY triggered when a client really connects/disconnects, so duplicate connect/disconnect messags
	 * are filtered
	 * 
	 * when this is triggered, we inform the admin clients
	 */
	IClientRegistrationListener cleanClientListener = new IClientRegistrationListener() {
		@Override
		public void onClientRegistrationChanged(Client c, boolean registered) {
			List<Client> admins = null;
			// copy to avoid threading problems
			synchronized (m_adminClients) {
				admins = new ArrayList<Client>(m_adminClients);
			}
			String msg = AdminServerProtocolAbstractor.createClientMessage(c.m_id, c.m_address, registered);
			// send the client-change to every admin client
			for (Client admin : admins) {
				sendToClient(m_clients.getServerClientByClientInfo(admin), msg);
			}
		}
	};

	
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
		log.info("admin client set sensor configuration: " + conf + " for sensor: " + sensorId + " to: " + data);
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
