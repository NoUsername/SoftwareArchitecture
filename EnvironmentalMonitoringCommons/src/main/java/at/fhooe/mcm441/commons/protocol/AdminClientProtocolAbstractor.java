package at.fhooe.mcm441.commons.protocol;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import at.fhooe.mcm441.commons.Configuration;
import at.fhooe.mcm441.commons.Configuration.SettingType;
import at.fhooe.mcm441.commons.network.Client;

/**
 * abstraction for the communication protocol between client and server (on client side)
 * 
 * @author Paul Klingelhuber
 */
public class AdminClientProtocolAbstractor extends ClientProtocolAbstractor implements IAdminClientCommands{
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
	
	private IAdminClientSideListener m_adminlistener;
	
	public AdminClientProtocolAbstractor(IMessageSender sender, IAdminClientSideListener listener) {
		super(sender, listener);
		m_adminlistener = listener;
	}
	
	@Override
	public boolean parseMessage(String msg) {
		if (!super.parseMessage(msg)) {
			try {
				JSONObject json = new JSONObject(msg);
				String type = json.getString("type");
				JSONObject data = json.getJSONObject("data");
				if (Protocol.TYPE_CLIENT.equals(type)) {
					String cId = data.getString(Protocol.FIELD_CLIENTID);
					String address = data.getString(Protocol.FIELD_CLIENTADDR);
					boolean connected = data.getBoolean(Protocol.FIELD_YESNO);
					Client client = new Client(cId, address);
					if (connected) {
						m_adminlistener.onClientConnected(client);
					} else {
						m_adminlistener.onClientDisconnected(client);
					}
				} else if (Protocol.TYPE_CONFIG.equals(type)) {
					
					Configuration conf = parseConfFromJson(data);
					m_adminlistener.onServerConfigurationItem(conf);
					
				} else if (Protocol.TYPE_SENSORCONFIG.equals(type)) {
					Configuration conf = parseConfFromJson(data);
					String sensorId = data.getString(Protocol.FIELD_SENSORID);
					m_adminlistener.onSensorConfigurationItem(sensorId, conf);
				} else {
					return false;
				}
			} catch (JSONException jse) {
				return false;	
			}
			
		}
		return true;
	}

	@Override
	public void setConfiguration(String confid, String data) {
		try {
			JSONObject inner = new JSONObject();
			inner.put(Protocol.FIELD_VALUETYPE, confid);
			inner.put(Protocol.FIELD_VALUE, data);
			
			m_endpoint.sendMessage(Protocol.createJsonContainer(Protocol.TYPE_CONFIG, inner));
		} catch (JSONException jse) {
			log.warn("could not build json", jse);
		}
	}

	@Override
	public void setSensorConfiguration(String sensorid, String confId, String value) {
		try {
			JSONObject inner = new JSONObject();
			inner.put(Protocol.FIELD_SENSORID, sensorid);
			inner.put(Protocol.FIELD_VALUETYPE, confId);
			inner.put(Protocol.FIELD_VALUE, value);
			
			m_endpoint.sendMessage(Protocol.createJsonContainer(Protocol.TYPE_SENSORCONFIG, inner));
		} catch (JSONException jse) {
			log.warn("could not build json", jse);
		}
	}
	
	private Configuration parseConfFromJson(JSONObject json) throws JSONException {
		String name = json.getString(Protocol.FIELD_NAME);
		String id = json.getString(Protocol.FIELD_CONFID);
		String value = json.getString(Protocol.FIELD_VALUE);
		String type = json.getString(Protocol.FIELD_VALUETYPE);
		
		SettingType t = SettingType.text;
		if ("bool".equals(type)) {
			t = SettingType.bool;
		} else if ("num".equals(type)) {
			t = SettingType.number;
		}
		return new Configuration(name, id, t, value);
	}

}

