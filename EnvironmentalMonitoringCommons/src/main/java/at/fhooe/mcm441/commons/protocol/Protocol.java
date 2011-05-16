package at.fhooe.mcm441.commons.protocol;

import org.json.JSONException;
import org.json.JSONObject;

public class Protocol {
	private Protocol() {}
	
	public static final String TYPE = "type";
	public static final String TYPE_REGISTER = "register";
	public static final String TYPE_SENSOR = "sensor";
	public static final String TYPE_SENSORDATA = "data";
	public static final String TYPE_BYE = "bye";
	
	////
	//// ADMIN MSGS
	////
	public static final String TYPE_CLIENT = "client";
	public static final String TYPE_CONFIG = "conf";
	public static final String TYPE_SENSORCONFIG = "sensorconf";
	
	public static final String DATA = "data";
	public static final String FIELD_SENSORID = "sid";
	
	public static final String FIELD_YESNO = "bool";
	public static final String FIELD_VALUETYPE = "vtype";
	public static final String FIELD_VALUE = "val";
	public static final String FIELD_DESCRIPTION = "desc";
	public static final String FIELD_DATATYPE = "unit";
	
	public static final String FIELD_CLIENTID = "clientid";
	public static final String FIELD_CLIENTADDR = "clientaddr";
	
	public static final String FIELD_NAME = "name";
	public static final String FIELD_CONFID = "confid";
	
	
	public static String createJsonContainer(String method, JSONObject data) throws JSONException {
		JSONObject json = new JSONObject();
		json.put(Protocol.TYPE, method);
		json.put(Protocol.DATA, data);
		return json.toString();
	}

}
