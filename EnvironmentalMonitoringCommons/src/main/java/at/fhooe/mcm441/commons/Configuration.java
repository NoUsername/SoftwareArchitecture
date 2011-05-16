package at.fhooe.mcm441.commons;

/**
 * information about a configuration item from the server
 * 
 * this should be used to create a graphical editing element in the client
 * when this element is changed, the client should send its id and the new data
 * to the server
 * 
 * @author Paul Klingelhuber
 */
public class Configuration {
	
	public static enum SettingType { text, bool, number };
	
	public Configuration(String displayName, String id, SettingType type,
			String value) {
		super();
		this.displayName = displayName;
		this.id = id;
		this.type = type;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return String.format("Conf {0} '{1}' type={2}, value={3}", id, displayName, type, value);
	}
	
	public String displayName;
	public String id;
	public SettingType type;
	public String value;

}
