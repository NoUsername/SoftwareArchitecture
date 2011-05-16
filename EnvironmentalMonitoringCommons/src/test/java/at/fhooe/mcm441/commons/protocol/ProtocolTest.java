package at.fhooe.mcm441.commons.protocol;

import junit.framework.TestCase;

import org.json.JSONObject;

/**
 * 
 * @author Paul Klingelhuber
 *
 */
public class ProtocolTest extends TestCase {
	
	
	public void testProtocolDataWrapper() throws Exception {
		String expectedString = "{\"data\":{\"a\":\"b\"},\"type\":\"bla\"}";
		
		JSONObject json = new JSONObject();
		json.put("a", "b");
		String s = Protocol.createJsonContainer("bla", json);
		
		assertEquals("we got: " + s + " but expected: " + expectedString, expectedString, s);
		
	}

}
