package at.fhooe.mcm441.client;

import org.slf4j.Logger;

import at.fhooe.mcm441.commons.network.IConnectionStatusListener;
import at.fhooe.mcm441.commons.protocol.AdminClientProtocolAbstractor;
import at.fhooe.mcm441.commons.protocol.IAdminClientSideListener;
import at.fhooe.mcm441.commons.protocol.IClientSideListener;

/**
 * Use this class to start a connection to the server, get notified about things and send commands to the server 
 * 
 * @author Paul Klingelhuber
 */
public class AdminConnection extends Connection {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
	
	/**
	 * protocol abstraction
	 */
	private AdminClientProtocolAbstractor adminProt;
	
	/**
	 * create and start the connection to the server
	 * 
	 * @param target target-host like "localhost"
	 * @param port target-port like 4444
	 * @param listener the client-side listener, this object will get notified about all the events from the server
	 * @throws Exception when an error occurs, an exception may be thrown
	 */
	public AdminConnection(String target, int port, IAdminClientSideListener listener, IConnectionStatusListener conListener)  throws Exception {
		super(target, port, listener, conListener);
	}
	
	/**
	 * override this to register the admin protocol listener
	 * @param listener, this now has to be an instance of IAdminClientSideListener
	 */
	@Override
	protected void createProtocolAbstractor(IClientSideListener listener) {
		adminProt = new AdminClientProtocolAbstractor(client, (IAdminClientSideListener)listener);
		// overwrite the parents-instance with the admin-protocol-abstractor
		prot = adminProt;
	}

	/**
	 * only needed to forward the packages to the protocol-parser
	 */
	@Override
	public void onNewPackage(String newPackage) {
		if (!prot.parseMessage(newPackage))
			log.warn("unknown command: " + newPackage);
	}
	
	/**
	 * register or deregister for a certain sensor
	 * 
	 * @param sensorId the id
	 * @param register true when you want to register, false if you want to deregister
	 */
	public void registerForSensor(String sensorId, boolean register) {
		prot.setRegistrationForSensor(sensorId, register);
	}
	
	/**
	 * configure settings of a sensor
	 * 
	 * @param sensorId
	 * @param confId
	 * @param confData
	 */
	public void setSensorConfig(String sensorId, String confId, String confData) {
		adminProt.setSensorConfiguration(sensorId, confId, confData);
	}
	
	/**
	 * configure settings from the server
	 * 
	 * @param confId
	 * @param confData
	 */
	public void setServerConfig(String confId, String confData) {
		adminProt.setConfiguration(confId, confData);
	}

}
