package at.fhooe.mcm441.sensor;

import java.net.InetAddress;
import java.util.Random;

import org.slf4j.Logger;

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
public class SensorApp implements IPackageListener
{
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this
			.getClass().getName());
	
	/**
	 * main method, starts the sensor
	 */
    public static void main( String[] args ) throws Exception
    {
    	new SensorApp();
	}
    
    /** connection */
    private NetworkServiceClient client;
    /** if false, data gets pushed */
    boolean isPoll = false;
    /** descriptive text */
    String description = "Some sensor";
    /** the datatype */
    String datatype = "°C";
    /** how long to wait between data pushes */
    int pushWaitTime = 1000;
    /** used for random stuff */
    Random r = new Random();
    
	public SensorApp() throws Exception {
		client = new NetworkServiceClient(this);
		client.connectAndStart(InetAddress.getByName("localhost"), 5555);
		
		isPoll = r.nextBoolean();
		
		String startMsg = SensorProtocol.createSensorInfoMsg(description, datatype, isPoll);
		
		client.sendMessage(startMsg);
		
		if (!isPoll) {
			pushWaitTime = 500 + r.nextInt(2000);
			// start pushing thread
			new Thread(new Runnable() {
				@Override
				public void run() {
					pushData();
				}
			}).start();
			log.info("started pushing sensor with push intervall of " + pushWaitTime);
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
		client.sendMessage(dataMsg);
	}
	
	/**
	 * creates the next data value
	 * @return
	 */
	private double getData() {
		return r.nextDouble();
	}
	
	/**
	 * infinite loop which pushes the data
	 */
	private void pushData() {
		while (true) {
			String dataMsg = SensorProtocol.createSensorDataMsg(getData());
			client.sendMessage(dataMsg);
			Util.sleep(pushWaitTime);
		}
	}
	
	
}
