package at.fhooe.mcm441.sensor;

import java.util.Random;

import org.slf4j.Logger;

import at.fhooe.mcm441.commons.util.Util;

public class SensorTester extends SensorApp {
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(SensorTester.class.getName());
	
	private static final boolean HARDCORETEST = true; // if this is true, not one but MANY clients are started
	private static final boolean LOGGING = !HARDCORETEST;
	
	private static final int STARTED_CLIENTS_COUNT = 10;
	private static final int MIN_STARTING_OFFSET = 200; // milliseconds
	
	private static final String HOST = "localhost";
	private static final int PORT = 4441;
	
	private static int msgsSentCount = 0;
	private static int clientsConnectedCount = 0;
	
	private static int instanceCount = 0;

	public static void main(String[] args) throws Exception {
		if (!HARDCORETEST) {
			new SensorTester();
		} else {
			hardCoreTest();
		}
	}
	
	@Override
	protected void beginCommunication() {
		// first we change the name to indicate the instance
		int count = instanceCount++;
		m_description = m_description + " (" + count + ")";
		super.beginCommunication();
		
	}
	
	public static void hardCoreTest() throws Exception {
		long started = System.currentTimeMillis();
		final Random r = new Random();
		for (int i=0; i<STARTED_CLIENTS_COUNT; i++) {
			new Thread(new Runnable() {
				public void run() {
					try {
						new SensorTester();
					} catch (Exception e) {
						e.printStackTrace();
					}
			}}).start();
			Thread.sleep(MIN_STARTING_OFFSET+r.nextInt(10));
			if (i%10 == 0)
				System.err.println(" WE ARE AT " + i + " of " + STARTED_CLIENTS_COUNT + " clients");
		}
		
		// log status:
		for (;;) {
			Util.sleep(5000);
			log.info("SENSORS CONNECTED INFO: " + clientsConnectedCount);
			log.info("TOTAL MSGS SENT: " + msgsSentCount);
			log.info("IN : " + (System.currentTimeMillis() - started) + " ms");
		}
	}
	
	public SensorTester() {
		super(HOST, PORT);
	}
	
	@Override
	protected void sendNewData(String msg) {
		msgsSentCount++;
		super.sendNewData(msg);
	}
	
	/**
	 * 
	 */
	@Override
	public void onConnectionEstablished() {
		clientsConnectedCount++;
		if (LOGGING)
			super.onConnectionEstablished();
	}

}
