package at.fhooe.mcm441.client;

import junit.framework.TestCase;
import at.fhooe.mcm441.commons.network.IConnectionStatusListener;
import at.fhooe.mcm441.commons.protocol.IClientSideListener;
import at.fhooe.mcm441.sensor.Sensor;

public class ConnectionTest extends TestCase implements IClientSideListener, IConnectionStatusListener {

	private Boolean connectionStatus = null;
	
	public void setUp() {
		connectionStatus = null;
	}
	
	/**
	 * this test will obviously be wrong, if you start the server before!
	 */
	public void testConnecitonFails() {
		try {
			Connection c = new Connection("localhost", 4444, this, this);
			// this connection should fail... wait for that:
			for (int i=0; i<10; i++) {
				// if we have a result before our max-wait-time, break
				if (connectionStatus != null)
					break;
				Thread.sleep(500);
			}
			
			assertTrue(connectionStatus != null && connectionStatus.booleanValue() == false);
		} catch (Exception e) {
			assertTrue("should not get an exception", false);
		}
	}

	@Override
	public void onSensorActivated(Sensor s) {
		
	}

	@Override
	public void onSensorDeactivated(String sensorId) {
		
	}

	@Override
	public void onNewSensorData(String sensorId, double value) {
		
	}

	@Override
	public void onConnectionEstablished() {
		connectionStatus = true;
	}

	@Override
	public void onConnectionLost() {
		connectionStatus = false;
	}
}
