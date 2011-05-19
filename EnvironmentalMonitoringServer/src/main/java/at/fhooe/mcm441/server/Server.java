package at.fhooe.mcm441.server;

import at.fhooe.mcm441.server.clients.ClientAbstraction;
import at.fhooe.mcm441.server.clients.ClientAbstractionPooled;
import at.fhooe.mcm441.server.output.HtmlOutput;
import at.fhooe.mcm441.server.preferences.Preferences;
import at.fhooe.mcm441.server.processing.ISensorDataListener;
import at.fhooe.mcm441.server.processing.ProcessingManager;
import at.fhooe.mcm441.server.sensors.SensorManager;

/**
 * the main class for the server
 * 
 * @author Paul Klingelhuber, Manuel Lachberger
 * 
 */
public class Server {

	private Server() {
	}

	private static ClientAbstraction m_clientAbstr = null;

	private static SensorManager m_sensorManager = null;
	
	private static Preferences m_preferences = null;
	
	private static ProcessingManager m_processing = null;

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
		
		m_processing = getProcessingManager();
		
		m_clientAbstr.startClientAbstraction();

		m_processing.register(m_clientAbstr);

		// m_preferences.register(Definitions.PREFIX_SENSORS_VISIBILITY,
		// m_clientAbstr);
		// inform the client abstraction about changed/new value
		// m_preferences.addNewPreference(Definitions.PREFIX_SENSORS_VISIBILITY,
		// "true");
	}
	
	public static ProcessingManager getProcessingManager(){
		if(m_processing == null){
			m_processing = new ProcessingManager();
		}
		return m_processing;
	}
	
	public static SensorManager getSensorManager() {
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

}
