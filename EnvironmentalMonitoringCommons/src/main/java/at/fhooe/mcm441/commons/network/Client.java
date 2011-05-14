package at.fhooe.mcm441.commons.network;

import java.util.UUID;

public class Client {
	public String m_id;
	public String m_address;
	
	public Client(String id, String addr) {
		m_id = id;
		m_address = addr;
	}
	
	public Client(String addr) {
		m_id = UUID.randomUUID().toString();
		m_address = addr;
	}
	
	@Override
	public String toString() {
		return String.format("Client {0} from {1}", m_id, m_address);
	}
}