package at.fhooe.mcm441.server;

import org.slf4j.Logger;

import at.fhooe.mcm441.commons.util.Util;
import at.fhooe.mcm441.server.clients.ClientAbstraction;
import at.fhooe.mcm441.server.clients.ClientAbstractionPooled;
import at.fhooe.mcm441.server.preferences.Preferences;
import at.fhooe.mcm441.server.sensors.ISensorStorage;
import at.fhooe.mcm441.server.sensors.SensorManager;

/**
 * the main class for the server
 * 
 * @author Paul Klingelhuber, Manuel Lachberger
 * 
 */
public class Server {
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(Server.class.getName());

	private Server() {
	}

	private static ClientAbstraction m_clientAbstr = null;

	private static SensorManager m_sensorManager = null;
	
	private static Preferences m_preferences = null;

	/**
	 * this should start up the server
	 */
	public static void initServer() {

		// probably some steps like:
		// - read config
		// - setup settings
		// - connect to sensors/let sensors connec
		// - start client abstraction

		m_preferences = getPreferences();
		
		m_sensorManager = getSensorManager();
		
		m_clientAbstr = getClientAbstraction();
		
		m_clientAbstr.startClientAbstraction();
		
		// periodic status information:
		while (true) {
			int clients = m_clientAbstr.getClientCount();
			int sensors = m_sensorManager.getAllSensors().size();
			log.info("there are currently " + clients + " clients connected and " + sensors + " sensors available");
			Util.trySleep(5000);
		}

		// m_preferences.register(Definitions.PREFIX_SENSORS_VISIBILITY,
		// m_clientAbstr);
		// inform the client abstraction about changed/new value
		// m_preferences.addNewPreference(Definitions.PREFIX_SENSORS_VISIBILITY,
		// "true");
	}
	
	/**
	 * the sensor manager isn't public (as sensorManager itself)
	 */
	private static SensorManager getSensorManager() {
		if (m_sensorManager == null) {
			m_sensorManager = new SensorManager();
		}
		return m_sensorManager;
	}

	public static ClientAbstraction getClientAbstraction() {
		if (m_clientAbstr == null) {
			m_clientAbstr = new ClientAbstractionPooled();
		}
		return m_clientAbstr;
	}

	public static Preferences getPreferences() {
		if (m_preferences == null) {
			m_preferences = new Preferences();
		}
		return m_preferences;
	}
	
	/******
	 * PUBLIC ACCESSOR METHODS:
	 *******/
	
	/**
	 * from here you get information about all the sensors that we have
	 */
	public static ISensorStorage getSensorStorage() {
		return m_sensorManager;
	}

}
