package at.fhooe.mcm441.server.clients;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;

import at.fhooe.mcm441.commons.network.Client;

/**
 * a container for storing/retrieving client objects
 * 
 * @author Paul Klingelhuber
 *
 */
public class ClientManager {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
	
	class DirtyClientList {
		public DirtyClientList() {
			dirty = false;
			list = new ArrayList<ServerClient>();
		}
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
		if (clients == null) {
			return null;
		}
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