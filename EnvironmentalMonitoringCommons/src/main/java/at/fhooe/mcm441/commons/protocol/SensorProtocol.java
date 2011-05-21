package at.fhooe.mcm441.commons.protocol;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import at.fhooe.mcm441.commons.network.Client;

/**
 * abstraction for the sensor protocol
 * 
 * @author Paul Klingelhuber
 *
 */
public class SensorProtocol {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
	private static final Logger slog = org.slf4j.LoggerFactory.getLogger(SensorProtocol.class.getName());
	
	/** gets informed about valid messages when parsing */
	private ISensorProtocolListener m_listener = null;
	
	/**
	 * 
	 * @param listener gets informed about valid messages when parsing
	 */
	public SensorProtocol(ISensorProtocolListener listener) {
		m_listener = listener;
	}
	
	/**
	 * parses a message and informs the listener about valid ones
	 * @param from
	 * @param msg
	 * @return
	 */
	public boolean parseMessage(Client from, String msg) {
		try {
			if ("".equals(msg)) {
				// often we get one last empty string when clients disconnect, handle this here
				return true;
			}
			
			JSONObject json = new JSONObject(msg);
			String type = json.getString("type");
			if (Protocol.TYPE_REGISTER.equals(type)) {
				JSONObject data = json.getJSONObject("data");
				String desc = data.getString(Protocol.FIELD_DESCRIPTION);
				String dataType = data.getString(Protocol.FIELD_VALUETYPE);
				boolean isPolling = data.getBoolean(Protocol.FIELD_YESNO);
				m_listener.onSensorInfo(from, desc, dataType, isPolling);
			} else if (Protocol.TYPE_SENSORDATA.equals(type)) {
				double val = json.getDouble(Protocol.FIELD_VALUE);
				m_listener.onSensorData(from, val);
			} else {
				// unknown msg
				return false;
			}
		} catch (JSONException jse) {
			log.warn("could not parse json '" + msg + "'", jse);
			return false;
		}
		return true;
	}
	
	/**
	 * to be called by sensor, what is returned should be sent to server
	 * @param data
	 * @return
	 */
	public static String createSensorDataMsg(double data) {
		try {
			JSONObject json = new JSONObject();
			json.put(Protocol.TYPE, Protocol.TYPE_SENSORDATA);
			json.put(Protocol.FIELD_VALUE, data);
			return json.toString();
		} catch (JSONException e) {
			slog.warn("cannot parse json", e);
			return null;
		}
	}
	
	/**
	 * to be called by sensor, what is returned should be sent to server
	 * @return
	 */
	public static String createSensorInfoMsg(String description, String dataType, boolean isPolling) {
		try {
			JSONObject data = new JSONObject();
			data.put(Protocol.FIELD_DESCRIPTION, description);
			data.put(Protocol.FIELD_VALUETYPE, dataType);
			data.put(Protocol.FIELD_YESNO, isPolling);
			
			return Protocol.createJsonContainer(Protocol.TYPE_REGISTER, data);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
