package at.fhooe.mcm441.server.clients;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import at.fhooe.mcm441.commons.network.Client;
import at.fhooe.mcm441.commons.network.IMultiClientNetworkEventsListener;
import at.fhooe.mcm441.commons.network.NetworkServiceClient;
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
	
	protected ClientManager m_clients;
	
	public ClientAbstraction() {
		// TODO give the client manager a client-unregistered callback object
		m_clients = new ClientManager(null);
		
		//=================================
		//REGISTER PREFERENCES
		//=================================
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
			log.warn("cannot find client " + data.m_id);
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
	
	private void broadcastToAllClients(String msg) {
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

	@Override
	public void onByeMessage(Client c) {
		onClientDisconnectes(c);
	}

}
