package at.fhooe.mcm441.sensor;

import java.net.InetAddress;
import java.util.Random;

import org.slf4j.Logger;

import at.fhooe.mcm441.commons.network.IPackageListener;
import at.fhooe.mcm441.commons.network.NetworkServiceClient;
import at.fhooe.mcm441.commons.protocol.SensorProtocol;
import at.fhooe.mcm441.commons.util.Util;

/**
 * Hello world!
 *
 */
public class SensorApp implements IPackageListener
{
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this
			.getClass().getName());
	
    public static void main( String[] args ) throws Exception
    {
    	SensorApp a = new SensorApp();
	}
    
    private NetworkServiceClient client;
    boolean isPoll = false;
    String description = "Some sensor";
    String datatype = "°C";
    int pushWaitTime = 1000;
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

	@Override
	public void onNewPackage(String newPackage) {
		String dataMsg = SensorProtocol.createSensorDataMsg(getData());
		client.sendMessage(dataMsg);
	}
	
	private double getData() {
		return r.nextDouble();
	}
	
	private void pushData() {
		while (true) {
			String dataMsg = SensorProtocol.createSensorDataMsg(getData());
			client.sendMessage(dataMsg);
			Util.sleep(pushWaitTime);
		}
	}
	
	
}
