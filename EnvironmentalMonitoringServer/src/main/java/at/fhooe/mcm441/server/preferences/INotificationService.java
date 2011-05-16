package at.fhooe.mcm441.server.preferences;

/**
 * these are the preferences
 * 
 * @author manuel
 * 
 */
public interface INotificationService {
	/**
	 * register the ChangeListener on the service
	 * 
	 * @param prefix
	 *            e.g. "sensors.visibility"
	 * @param listener
	 *            the listener object
	 * @return true in case of success
	 */
	public boolean register(String prefix, IChangeListener listener);

	/**
	 * unregisters the specified changeListener from the service
	 * 
	 * @param prefix
	 *            e.g. "sensors.visibility"
	 * @param listener
	 *            the listener object
	 * @return true in case of success
	 */
	public boolean unregister(String prefix, IChangeListener listener);
}
