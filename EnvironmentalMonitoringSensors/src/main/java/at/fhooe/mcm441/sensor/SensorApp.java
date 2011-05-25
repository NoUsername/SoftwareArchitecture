package at.fhooe.mcm441.sensor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import org.slf4j.Logger;

import at.fhooe.mcm441.commons.network.IConnectionStatusListener;
import at.fhooe.mcm441.commons.network.IPackageListener;
import at.fhooe.mcm441.commons.network.NetworkServiceClient;
import at.fhooe.mcm441.commons.protocol.SensorProtocol;
import at.fhooe.mcm441.commons.util.Util;

/**
 * Sensor class, when started a random sensor is created
 * it depends on a random value if it is a pull or push sensor
 * 
 * push sensors choose their pushing time also by random
 *
 */
public class SensorApp implements IPackageListener, IConnectionStatusListener
{
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this
			.getClass().getName());
	
	/**
	 * main method, starts the sensor
	 */
    public static void main( String[] args ) throws Exception
    {
    	String host = "localhost";
    	int port = 4441;
    	if (args.length > 0) {
    		host = args[0];
    	}
    	
    	if (args.length > 1) {
    		port = Integer.parseInt(args[1]);
    	}
    	
    	new SensorApp(host, port);
	}
    
    /** connection */
    private NetworkServiceClient m_client;
    /** if false, data gets pushed */
    private boolean m_isPoll = false;
    /** descriptive text */
    protected String m_description = "Some sensor";
    /** the datatype */
    private String m_datatype = "°C";
    /** how long to wait between data pushes */
    private int m_pushWaitTime = 1000;
    /** used for random stuff */
    private Random m_r = new Random();
    /** as long as this is true, the sensor runs */
    private boolean m_running = true;
    
    /** list of possible descriptions */
    private final String[] m_possibleDescriptions = new String[]{"temperature sensor",
    		"light sensor", "sarkasm sensor", "laugh-o-meter", "danger-of-falling-asleep-o-meter",
    		"chance-of-rain-sensor", "worth-it-ness-sensor", "love-sensor"};
    private final String[] m_datatypes = new String[]{"°C",
    		"lux", "dieters", "laughs", "ZZZs",
    		"wetness", "worthness", "<3"};
    
	public SensorApp(String targetHost, int port) {
		
		m_client = new NetworkServiceClient(this, this);
		
		try {
			m_client.connectAndStart(InetAddress.getByName(targetHost), port);
		} catch (UnknownHostException e) {
			log.warn("cannot resolve target host " + targetHost);
		}
		
		m_isPoll = m_r.nextBoolean();
		
		int idx = m_r.nextInt(m_possibleDescriptions.length);
		m_description = m_possibleDescriptions[idx];
		m_datatype = m_datatypes[idx];
		
		beginCommunication();
	}
	
	
	/**
	 * sends the initial info (name, ...)
	 * and starts the push thread if this is a push sensor
	 */
	protected void beginCommunication() {
		String startMsg = SensorProtocol.createSensorInfoMsg(m_description, m_datatype, m_isPoll);
		
		m_client.sendMessage(startMsg);
		
		if (!m_isPoll) {
			m_pushWaitTime = 500 + m_r.nextInt(2000);
			// start pushing thread
			new Thread(new Runnable() {
				@Override
				public void run() {
					pushData();
				}
			}).start();
			log.info("started pushing sensor with push intervall of " + m_pushWaitTime);
		} else {
			log.info("started polling sensor...");
		}
	}

	/**
	 * when the sensor gets ANY data, it sent, we send back new sensordata
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void onNewPackage(String newPackage) {
		String dataMsg = SensorProtocol.createSensorDataMsg(getData());
		sendNewData(dataMsg);
	}
	
	/**
	 * creates the next data value
	 * @return
	 */
	private double getData() {
		return m_r.nextDouble();
	}
	
	/**
	 * infinite loop which pushes the data
	 */
	private void pushData() {
		while (m_running) {
			String dataMsg = SensorProtocol.createSensorDataMsg(getData());
			sendNewData(dataMsg);
			Util.sleep(m_pushWaitTime);
		}
	}
	
	/**
	 * actually sends sth to the server
	 * @param msg
	 */
	protected void sendNewData(String msg) {
		m_client.sendMessage(msg);
	}

	@Override
	public void onConnectionEstablished() {
		log.info("connection successfully established");
	}

	@Override
	public void onConnectionLost() {
		log.warn("we lost the connection, maybe the server shut down. Quitting...");
		m_running = false;
	}
	
}
