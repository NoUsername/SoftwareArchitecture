package at.fhooe.mcm441.server.clients;

import at.fhooe.mcm441.commons.network.Client;
import at.fhooe.mcm441.commons.network.NetworkServiceClient;
import at.fhooe.mcm441.commons.protocol.IMessageSender;

public class ServerClient {
	private Client m_client;
	private NetworkServiceClient m_con;
	
	public ServerClient(Client data, NetworkServiceClient connection) {
		m_client = data;
		m_con = connection;
	}
	
	public Client getClientInfo() {
		return m_client;
	}
	
	public IMessageSender getClientConnection() {
		return m_con;
	}
	
	public void tryClose() {
		if (m_con != null)
			m_con.stop();
	}

}
