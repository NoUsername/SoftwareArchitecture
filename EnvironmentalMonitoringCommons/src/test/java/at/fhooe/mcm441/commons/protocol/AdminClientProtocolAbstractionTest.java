package at.fhooe.mcm441.commons.protocol;

import at.fhooe.mcm441.commons.Configuration;
import at.fhooe.mcm441.commons.network.Client;
import at.fhooe.mcm441.sensor.Sensor;
import junit.framework.TestCase;

public class AdminClientProtocolAbstractionTest extends TestCase {
	
	public static String receiveMsg = "{\"data\":{\"val\":\"somedata\",\"sid\":\"iamasensorid\",\"vtype\":\"someconf\"},\"type\":\"sensorconf\"}";
	
	public static String testMsg1 = "{\"data\":{\"clientid\":\"someclient\",\"clientaddr\":\"10.0.0.1\",\"bool\":true},\"type\":\"client\"}";
	public static String testMsg2 = "";
	public static String testMsg3 = "";
	public static String testMsg4 = "";
	
	protected TestAdminClientSideListener listener;
	protected AdminClientProtocolAbstractor prot;
	
	public void setUp() {
		listener = new TestAdminClientSideListener();
		prot = new AdminClientProtocolAbstractor(new TestMessageSender(), listener);
	}
	
	public void testClientConnected() {
		prot.parseMessage(testMsg1);
		assertNotNull(listener.lastClient);
		assertEquals(listener.lastClient.m_id, "someclient");
		assertEquals(listener.lastClient.m_address, "10.0.0.1");		
	}
	
	public void testSetSensorConf() {
		prot.setSensorConfiguration("iamasensorid", "someconf", "somedata");
		assertEquals(receiveMsg, TestMessageSender.lastMsg);
	}
	
}


/*********************************
 * 
 * Test implementation classes
 * 
 */

class TestMessageSender implements IMessageSender {
	public static String lastMsg = null;
	
	@Override
	public boolean sendMessage(String msg) {
		lastMsg = msg;
		return true;
	}	
}

class TestAdminClientSideListener implements IAdminClientSideListener {
	public static Sensor sensorActivated = null;
	public static String sensorDeactivated = null;
	public static String lastSensorData = null;
	public static double lastSensorDataValue = 0;
	public static Client lastClient = null;
	public static Client lastClientDisconnected = null;
	public static Configuration lastConf = null;
	public static String lastSensorConfId = null;
	public static Configuration lastSensorConf = null;
	
	
	public static void reset() {
		sensorActivated = null;
		sensorDeactivated = null;
		lastSensorData = null;
		lastSensorDataValue = 0;
		lastClient = null;
		lastClientDisconnected = null;
		lastConf = null;
		lastSensorConfId = null;
		lastSensorConf = null;
	}

	@Override
	public void onSensorActivated(Sensor s) {
		sensorActivated = s;
	}

	@Override
	public void onSensorDeactivated(String sensorId) {
		sensorDeactivated = sensorId;
	}

	@Override
	public void onNewSensorData(String sensorId, double value) {
		lastSensorData = sensorId;
		lastSensorDataValue = value;
	}

	@Override
	public void onSensorConfigurationItem(String sensorId, Configuration conf) {
		lastSensorConfId = sensorId;
		lastSensorConf = conf;
	}

	@Override
	public void onServerConfigurationItem(Configuration conf) {
		lastConf = conf;
	}

	@Override
	public void onClientConnected(Client client) {
		lastClient = client;
	}

	@Override
	public void onClientDisconnected(Client client) {
		lastClientDisconnected = client;
	}
	
}
