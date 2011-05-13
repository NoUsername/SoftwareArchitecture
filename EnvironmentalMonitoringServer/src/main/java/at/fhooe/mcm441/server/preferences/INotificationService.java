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
	 *            e.g. �sensors.visibility�
	 * @param listener
	 *            the object
	 * @return true in case of success
	 */
	public boolean register(String prefix, IChangeListener listener);
}
