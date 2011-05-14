package at.fhooe.mcm441.server;

import at.fhooe.mcm441.server.clients.ClientAbstraction;

/**
 * 
 * @author Paul Klingelhuber
 *
 */
public class Server {
	
	private Server() {
	}
	
	private static ClientAbstraction m_clientAbstr;
	
	/**
	 * this should start up the server
	 */
	public static void initServer() {
		
		// probably some steps like:
		// - read config
		// - setup settings
		// - connect to sensors/let sensors connec
		// - start client abstraction
		
		m_clientAbstr = new ClientAbstraction();
	}
	
	
	
	public static ClientAbstraction getClientAbstraction() {
		return m_clientAbstr;
	}

}
