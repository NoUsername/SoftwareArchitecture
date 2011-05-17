package at.fhooe.mcm441.server.clients;

import org.slf4j.Logger;

import at.fhooe.mcm441.commons.network.Client;
import at.fhooe.mcm441.commons.network.IMultiClientNetworkListener;
import at.fhooe.mcm441.commons.network.MultiClientNetworkService;
import at.fhooe.mcm441.commons.protocol.ServerProtocolAbstractor;
import at.fhooe.mcm441.server.Server;
import at.fhooe.mcm441.server.utility.Definitions;

public class ServerNetworkConnection implements IMultiClientNetworkListener {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
	
	private ClientAbstraction m_clients;
	private ServerProtocolAbstractor m_protocol;
	
	public ServerNetworkConnection(ClientAbstraction clients) {
		m_clients = clients;
		m_protocol = new ServerProtocolAbstractor(clients);
		MultiClientNetworkService server = new MultiClientNetworkService(this, m_clients);
		String port = Server.getPreferences().getValue(Definitions.PREFIX_SERVER_PORT);
		int p = Integer.parseInt(port);
		server.startListening(p);
		log.debug("server started on port " + port);
	}

	@Override
	public void onNewPackage(Client from, String newPackage) {
		
		log.trace("client " + from.m_id + " said: " + newPackage);
		m_protocol.parseMessage(from, newPackage);
		
	}

}
