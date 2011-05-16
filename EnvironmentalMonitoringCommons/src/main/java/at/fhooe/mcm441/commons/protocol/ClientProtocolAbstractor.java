package at.fhooe.mcm441.commons.protocol;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import at.fhooe.mcm441.sensor.Sensor;

/**
 * abstraction for communication with the server
 * 
 * @author Paul Klingelhuber
 *
 */
public class ClientProtocolAbstractor implements IClientCommands {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
	
	protected IMessageSender m_endpoint;
	private IClientSideListener m_listener;
	
	public ClientProtocolAbstractor(IMessageSender sender, IClientSideListener listener) {
		m_endpoint = sender;
		m_listener = listener;
	}
	
	public boolean parseMessage(String msg) {
		try {
			JSONObject json = new JSONObject(msg);
			String type = json.getString("type");
			JSONObject data = json.getJSONObject("data");
			if (Protocol.TYPE_SENSOR.equals(type)) {
				String sId = data.getString(Protocol.FIELD_SENSORID);
				boolean activated = data.getBoolean(Protocol.FIELD_YESNO);
				if (activated) {
					String desc = data.getString(Protocol.FIELD_DESCRIPTION);
					String units = data.getString(Protocol.FIELD_DATATYPE);
					
					// we set polling to false and value to 0.0 because we don't know
					// any of that
					Sensor s = new Sensor(sId, desc, false, 0.0, units);
					
					m_listener.onSensorActivated(s);
				} else {
					m_listener.onSensorDeactivated(sId);
				}
			} else if (Protocol.TYPE_SENSORDATA.equals(type)) {
				String sId = data.getString("id");
				double val = data.getDouble("value");
				m_listener.onNewSensorData(sId, val);
			} else {
				// unknown msg
				return false;
			}
		} catch (JSONException jse) {
			log.warn("could not parse json", jse);
			return false;
		}
		return true;
	}

	@Override
	public void setRegistrationForSensor(String sensorId, boolean registered) {
		try {
			JSONObject inner = new JSONObject();
			inner.put(Protocol.FIELD_SENSORID, sensorId);
			inner.put(Protocol.FIELD_YESNO, registered);
			
			m_endpoint.sendMessage(Protocol.createJsonContainer(Protocol.TYPE_REGISTER, inner));
		} catch (JSONException jse) {
			log.warn("could not build json", jse);
		}
	}
	
}
