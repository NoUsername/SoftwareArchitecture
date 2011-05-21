package at.fhooe.mcm441.server.clients;

import org.slf4j.Logger;

import at.fhooe.mcm441.commons.network.Client;
import at.fhooe.mcm441.commons.network.IMultiClientNetworkEventsListener;
import at.fhooe.mcm441.commons.network.IMultiClientNetworkListener;
import at.fhooe.mcm441.commons.network.MultiClientNetworkService;
import at.fhooe.mcm441.commons.network.NetworkServiceClient;
import at.fhooe.mcm441.commons.protocol.AdminServerProtocolAbstractor;
import at.fhooe.mcm441.commons.protocol.ServerProtocolAbstractor;
import at.fhooe.mcm441.commons.util.Definitions;
import at.fhooe.mcm441.server.Server;

public class ServerNetworkConnection implements IMultiClientNetworkListener {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
	
	private ClientAbstraction m_clients;
	private ServerProtocolAbstractor m_protocol;
	private AdminServerProtocolAbstractor m_adminProtocol;
	
	public ServerNetworkConnection(final ClientAbstraction clients) {
		m_clients = clients;
		m_protocol = new ServerProtocolAbstractor(clients);
		m_adminProtocol = new AdminServerProtocolAbstractor(clients);
		MultiClientNetworkService server = new MultiClientNetworkService(this, m_clients);
		String port = Server.getPreferences().getValue(Definitions.PREFIX_SERVER_PORT);
		int p = Integer.parseInt(port);
		server.startListening(p);
		log.info("server started on port " + port);
		
		// now start the port for the admins:
		MultiClientNetworkService adminServer = new MultiClientNetworkService(new IMultiClientNetworkListener() {
			@Override
			public void onNewPackage(Client from, String newPackage) {
				onNewAdminClientPackage(from, newPackage);
			}
		},
		new IMultiClientNetworkEventsListener() {
			@Override
			public void onNewClient(Client c, NetworkServiceClient sc) {
				clients.onNewAdminClient(c, sc);
			}
			
			@Override
			public void onClientDisconnectes(Client c) {
				clients.onAdminClientDisconnects(c);
			}
		}
		);
		
		// admin clients must connect to one port number above the normal clients
		adminServer.startListening(p + 1);
		log.info("admin server started on port " + (p+1));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNewPackage(Client from, String newPackage) {
		log.trace("client " + from.m_id + " said: " + newPackage);
		m_protocol.parseMessage(from, newPackage);
	}
	
	public void onNewAdminClientPackage(Client from, String newPackage) {
		log.trace("admin client " + from.m_id + " said: " + newPackage);
		m_adminProtocol.parseMessage(from, newPackage);
	}

}
