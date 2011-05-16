package at.fhooe.mcm441.server.clients;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;

import at.fhooe.mcm441.commons.network.Client;
import at.fhooe.mcm441.commons.network.IMultiClientNetworkEventsListener;
import at.fhooe.mcm441.commons.network.NetworkServiceClient;
import at.fhooe.mcm441.commons.protocol.IServerCommandListener;
import at.fhooe.mcm441.commons.protocol.ServerProtocolAbstractor;
import at.fhooe.mcm441.sensor.Sensor;
import at.fhooe.mcm441.server.preferences.IChangeListener;
import at.fhooe.mcm441.server.preferences.Preferences;

public class ClientAbstraction implements IChangeListener, IServerCommandListener, IMultiClientNetworkEventsListener {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
	
	private boolean m_isStarted = false;
	
	private ClientManager m_clients;
	
	public ClientAbstraction() {
		// TODO register @ preferences here!
		// TODO give the client manager a client-unregistered callback object
		m_clients = new ClientManager(null);
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
		List<ServerClient> clients = m_clients.getClientsForSensor(s.ident);
		// TODO create the message object
		sendToClients(clients, "TODOOOO");
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
			m_clients.removeClient(sc);
		} else {
			log.warn("cannot find client " + data.m_id);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(String key, String msg) {
		if (key.startsWith(Preferences.PREFIX_SENSOR_VISIBILITY)) {
			// notify all clients
			String sensorId = key.replace(Preferences.PREFIX_SENSOR_VISIBILITY + ".", "");
			String msgToSend = ServerProtocolAbstractor.createSensorVisibilityMessage(sensorId, "TODO", "TODO", Boolean.parseBoolean(msg));
			broadcastToAllClients(msgToSend);
		}
	}
	
	private void broadcastToAllClients(String msg) {
		// TODO: implement broadcasting, consider the following:
		// don't block the caller, because this could take some time
		// are clients where we lose the connection already handled?
		
		// XXX this dummy implementation BLOCKS THE WHOLE TIME!!!
		List<ServerClient> all = m_clients.getAllClients();
		for (ServerClient sc : all) {
			sc.getClientConnection().sendMessage(msg);
		}
	}
	
	private void sendToClients(List<ServerClient> clients, String msg) {
		// TODO: implement broadcasting, consider the following:
		// don't block the caller, because this could take some time
		// are clients where we lose the connection already handled?
		
		// XXX this dummy implementation BLOCKS THE WHOLE TIME!!!
		for (ServerClient sc : clients) {
			sc.getClientConnection().sendMessage(msg);
		}
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
		if (status)
			m_clients.onClientRegisteresForSensor(sc, sensorId);
		else
			m_clients.onClientUnregistersForSensor(sc, sensorId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onAdminConfigCommand(Client c, Object conf, Object data) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onAdminSensorConfigCommand(Client c, String sensorId,
			Object conf, Object data) {
		// TODO Auto-generated method stub
		
	}

}

/**
 * a container for storing/retrieving client objects
 * 
 * @author Paul Klingelhuber
 *
 */
class ClientManager {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
	
	class DirtyClientList {
		public DirtyClientList() {}
		public DirtyClientList(List<ServerClient> l) {
			dirty = false;
			list = l;
		}
		boolean dirty;
		List<ServerClient> list;
	}
	
	private Map<String, DirtyClientList> m_clientsBySensors;
	private List<ServerClient> m_allClients;
	private Map<Client, ServerClient> m_allByData;
	private IClientRegistrationListener m_listener;
	
	
	public ClientManager(IClientRegistrationListener listener) {
		m_listener = listener;
		m_clientsBySensors = new HashMap<String, ClientManager.DirtyClientList>();
		m_allByData = new HashMap<Client, ServerClient>();
		m_allClients = new ArrayList<ServerClient>();
	}
	
	public void addClient(ServerClient c) {
		if (m_listener != null)
			m_listener.onClientRegistrationChanged(c.getClientInfo(), true);
		
		if (!m_allByData.containsKey(c.getClientInfo())) {
			m_allClients.add(c);
			m_allByData.put(c.getClientInfo(), c);
		} else {
			log.warn("client " + c.getClientInfo().m_id + " already known");
		}
	}
	
	public ServerClient getServerClientByClientInfo(Client info) {
		return m_allByData.get(info);
	}
	
	public void removeClient(ServerClient c) {
		if (m_listener != null)
			m_listener.onClientRegistrationChanged(c.getClientInfo(), false);
		
		m_allClients.remove(c);
		m_allByData.remove(c.getClientInfo());
		
		// we removed a client, instead of searching through all the sensor lists now
		// we just set them all to dirty
		for (Entry<String, DirtyClientList> entry : m_clientsBySensors.entrySet()) {
			entry.getValue().dirty = true;
		}
	}
	
	public void onClientRegisteresForSensor(ServerClient c, String sensorId) {
		if (!m_allClients.contains(c)) {
			addClient(c);
		}
		
		List<ServerClient> sensorsClients = getCleanedClientsBySensor(sensorId);
		if (sensorsClients == null) {
			sensorsClients = new ArrayList<ServerClient>();
			m_clientsBySensors.put(sensorId, new DirtyClientList(sensorsClients));
		}
		
		if (!sensorsClients.contains(c))
			sensorsClients.add(c);
	}
	
	public void onClientUnregistersForSensor(ServerClient c, String sensorId) {
		if (!m_allClients.contains(c)) {
			addClient(c);
		}
		
		List<ServerClient> sensorsClients = getCleanedClientsBySensor(sensorId);
		if (sensorsClients == null) {
			sensorsClients = new ArrayList<ServerClient>();
			m_clientsBySensors.put(sensorId, new DirtyClientList(sensorsClients));
		} else {
			if (sensorsClients.contains(c)) {
				sensorsClients.remove(c);
			}
		}
	}
	
	public List<ServerClient> getClientsForSensor(String sensorId) {
		return getCleanedClientsBySensor(sensorId);
	}
	
	public  List<ServerClient> getAllClients() {
		return m_allClients;
	}
	
	/**
	 * gets a list of clients for the given sensor
	 * if the list is dirty, it cleans it
	 * @param sensorId
	 * @return
	 */
	private List<ServerClient> getCleanedClientsBySensor(String sensorId) {
		DirtyClientList clients = m_clientsBySensors.get(sensorId);
		if (clients.dirty) {
			cleanDirtyList(clients);
		}
		return clients.list;
	}
	
	/**
	 * cleans a client-list
	 * this means to look if there is a client in there which has already disconnected
	 * @param list
	 */
	private void cleanDirtyList(DirtyClientList list) {
		List<ServerClient> removeThose = new ArrayList<ServerClient>();
		for (ServerClient s : list.list) {
			if (!m_allClients.contains(s)) {
				removeThose.add(s);
			}
		}
		list.list.removeAll(removeThose);
		list.dirty = false;
	}
	
}
