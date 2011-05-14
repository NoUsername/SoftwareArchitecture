package at.fhooe.mcm441.server.preferences;

/**
 * update the listener
 * 
 * @author manuel
 * 
 */
public interface IChangeListener {
	/**
	 * update
	 * @param key
	 *            "sensors.visibility.id4"
	 * @param msg
	 *            the actual value "42"
	 */
	public void update(String key, String msg);
}