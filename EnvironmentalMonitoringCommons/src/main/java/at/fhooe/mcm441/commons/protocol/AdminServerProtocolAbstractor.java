package at.fhooe.mcm441.commons.protocol;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import at.fhooe.mcm441.commons.Configuration;
import at.fhooe.mcm441.commons.network.Client;

/**
 * abstraction for the communication protocol on server side
 * 
 * @author Paul Klingelhuber
 *
 */
public class AdminServerProtocolAbstractor extends ServerProtocolAbstractor {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
	private static final Logger slog = org.slf4j.LoggerFactory.getLogger("static" + AdminServerProtocolAbstractor.class.getName());
	
	public AdminServerProtocolAbstractor(IServerCommandListener listener) {
		super(listener);
	}
	
	public boolean parseMessage(Client from, String msg) {
		if (!super.parseMessage(from, msg)) {
			try {
				JSONObject json = new JSONObject(msg);
				String type = json.getString("type");
				JSONObject data = json.getJSONObject("data");
				
				if (Protocol.TYPE_SENSORCONFIG.equals(type)) {
					String sId = data.getString(Protocol.FIELD_SENSORID);
					String confId = data.getString(Protocol.FIELD_VALUETYPE);
					String value = data.getString(Protocol.FIELD_VALUE);
					m_listener.onAdminSensorConfigCommand(from, sId, confId, value);
				} else if (Protocol.TYPE_CONFIG.equals(type)) {
					String confId = data.getString(Protocol.FIELD_VALUETYPE);
					String value = data.getString(Protocol.FIELD_VALUE);
					m_listener.onAdminConfigCommand(from, confId, value);
				} else {
					// unknown msg
					return false;
				}
			} catch (JSONException jse) {
				log.warn("could not parse json '" + msg + "'", jse);
				return false;
			}
		}
		// either parsed now, or already parsed by parent
		return true;
	}
	
	public static String createClientMessage(String clientId, String clientAddr, boolean connected) {
		try {
			JSONObject inner = new JSONObject();
			inner.put(Protocol.FIELD_CLIENTID, clientId);
			inner.put(Protocol.FIELD_CLIENTADDR, clientAddr);
			inner.put(Protocol.FIELD_YESNO, connected);
			
			return Protocol.createJsonContainer(Protocol.TYPE_CLIENT, inner);
		} catch (JSONException jse) {
			slog.warn("could not build json", jse);
			return null;
		}
	}
	
	public static String createConfigMessage(Configuration config) {
		try {
			JSONObject inner = new JSONObject();
			writeConfigurationToJson(config, inner);
			
			return Protocol.createJsonContainer(Protocol.TYPE_CONFIG, inner);
		} catch (JSONException jse) {
			slog.warn("could not build json", jse);
			return null;
		}
	}
	
	public static String createSensorConfigMessage(String sensorid, Configuration config) {
		try {
			JSONObject inner = new JSONObject();
			inner.put(Protocol.FIELD_SENSORID, sensorid);
			writeConfigurationToJson(config, inner);
			
			return Protocol.createJsonContainer(Protocol.TYPE_SENSORCONFIG, inner);
		} catch (JSONException jse) {
			slog.warn("could not build json", jse);
			return null;
		}
	}
	
	public static boolean writeConfigurationToJson(Configuration config, JSONObject json) {
		try {
			json.put(Protocol.FIELD_NAME, config.displayName);
			json.put(Protocol.FIELD_VALUE, config.value);
			json.put(Protocol.FIELD_CONFID, config.id);
			switch (config.type) {
			case bool:
				json.put(Protocol.FIELD_VALUETYPE, "bool");
				break;
			case text:
				json.put(Protocol.FIELD_VALUETYPE, "text");
				break;
			case number:
				json.put(Protocol.FIELD_VALUETYPE, "num");
				break;
			}
			return true;
		} catch (JSONException jse) {
			slog.warn("could not build json", jse);
			return false;
		}
	}

}
