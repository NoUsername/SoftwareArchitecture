package at.fhooe.mcm441.server.preferences;

/**
 * the preference service where the new configuration values can be registered
 * with prefix, key and value
 * 
 * @author manuel
 * 
 */
public interface IPreferencenWriter {

	/**
	 * adds a new preference to the service
	 * 
	 * @param prefix
	 *            the prefix to specify the preference (e.g. "server.port" )
	 * @param value
	 *            the value for the specified prefix
	 * @return true -> success, false -> error
	 */
	public boolean addNewPreference(String prefix, String value);

	/**
	 * updates the stored preference value with the given prefix to the new
	 * value
	 * 
	 * @param prefix
	 *            the prefix you want to update
	 * @param value
	 *            the new value for the prefix
	 * @return true -> success, false -> error
	 */
	public boolean updatePreference(String prefix, String value);

}
