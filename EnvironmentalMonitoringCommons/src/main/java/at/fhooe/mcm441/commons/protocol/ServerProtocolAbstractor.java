package at.fhooe.mcm441.commons.protocol;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import at.fhooe.mcm441.commons.network.Client;

/**
 * abstraction for the communication protocol on server side
 * 
 * @author Paul Klingelhuber
 *
 */
public class ServerProtocolAbstractor {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
	private static final Logger slog = org.slf4j.LoggerFactory.getLogger("static" + ServerProtocolAbstractor.class.getName());
	
	/** successfully parsed command messages are reported to this object */
	protected IServerCommandListener m_listener;
	
	public ServerProtocolAbstractor(IServerCommandListener listener) {
		m_listener = listener;
	}
	
	public boolean parseMessage(Client from, String msg) {
		try {
			if ("".equals(msg)) {
				// often we get one last empty string when clients disconnect, handle this here
				m_listener.onByeMessage(from);
				return true;
			}
			
			JSONObject json = new JSONObject(msg);
			String type = json.getString("type");
			JSONObject data = json.getJSONObject("data");
			if (Protocol.TYPE_REGISTER.equals(type)) {
				String sId = data.getString(Protocol.FIELD_SENSORID);
				boolean activated = data.getBoolean(Protocol.FIELD_YESNO);
				m_listener.onRegisterForSensor(from, sId, activated);
			} else if (Protocol.TYPE_BYE.equals(type)) {
				m_listener.onByeMessage(from);
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
	
	public static String createSensorVisibilityMessage(String sensorid, String description, String datatype, boolean visible) {
		try {
			JSONObject inner = new JSONObject();
			inner.put(Protocol.FIELD_SENSORID, sensorid);
			inner.put(Protocol.FIELD_YESNO, visible);
			if (visible) {
				inner.put(Protocol.FIELD_DATATYPE, datatype);
				inner.put(Protocol.FIELD_DESCRIPTION, description);
			} else {
				// no longer visible, doesn't need any more data
			}
			
			return Protocol.createJsonContainer(Protocol.TYPE_SENSOR, inner);
		} catch (JSONException jse) {
			slog.warn("could not build json", jse);
			return null;
		}
	}
	
	public static String createSensorDataMessage(String sensorid, double value) {
		try {
			JSONObject inner = new JSONObject();
			inner.put(Protocol.FIELD_SENSORID, sensorid);
			inner.put(Protocol.FIELD_VALUE, value);
			
			return Protocol.createJsonContainer(Protocol.TYPE_SENSORDATA, inner);
		} catch (JSONException jse) {
			slog.warn("could not build json", jse);
			return null;
		}
	}

}
