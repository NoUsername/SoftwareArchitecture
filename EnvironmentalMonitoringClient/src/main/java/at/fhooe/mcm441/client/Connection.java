package at.fhooe.mcm441.client;

import java.net.InetAddress;

import org.slf4j.Logger;

import at.fhooe.mcm441.commons.network.IConnectionStatusListener;
import at.fhooe.mcm441.commons.network.IPackageListener;
import at.fhooe.mcm441.commons.network.NetworkServiceClient;
import at.fhooe.mcm441.commons.protocol.ClientProtocolAbstractor;
import at.fhooe.mcm441.commons.protocol.IClientSideListener;

/**
 * Use this class to start a connection to the server, get notified about things and send commands to the server 
 * 
 * @author Paul Klingelhuber
 */
public class Connection implements IPackageListener {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
	
	/**
	 * protocol abstraction
	 */
	protected ClientProtocolAbstractor m_prot;
	
	protected NetworkServiceClient m_client;
	
	/**
	 * create and start the connection to the server
	 * 
	 * @param target target-host like "localhost"
	 * @param port target-port like 4444
	 * @param listener the client-side listener, this object will get notified about all the events from the server
	 * @throws Exception when an error occurs, an exception may be thrown
	 */
	public Connection(String target, int port, IClientSideListener listener, IConnectionStatusListener conListener)  throws Exception {
		m_client = new NetworkServiceClient(this, conListener);
		createProtocolAbstractor(listener);
		m_client.connectAndStart(InetAddress.getByName(target), port);
		
	}
	
	/**
	 * this can be overwritten, if a subclass wants to bind another protocol
	 * abstractor
	 * @param listener
	 */
	protected void createProtocolAbstractor(IClientSideListener listener) {
		m_prot = new ClientProtocolAbstractor(m_client, listener);
	}

	/**
	 * only needed to forward the packages to the protocol-parser
	 */
	@Override
	public void onNewPackage(String newPackage) {
		//log.info("server said: " + newPackage);
		if (!m_prot.parseMessage(newPackage))
			log.warn("unknown command: " + newPackage);
	}
	
	/**
	 * register or deregister for a certain sensor
	 * 
	 * @param sensorId the id
	 * @param register true when you want to register, false if you want to deregister
	 */
	public void registerForSensor(String sensorId, boolean register) {
		m_prot.setRegistrationForSensor(sensorId, register);
	}
	

	/**
	 * shuts down the connection
	 */
	public void close() {
		m_prot.logoff();
		m_client.stop();		
	}

}
