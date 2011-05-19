package at.fhooe.mcm441.server.processing;

/**
 * the listener service where listeners can (un)subscribe
 * 
 * @author Manuel Lachberger
 * 
 */
public interface ISensorListenerService {
	/**
	 * register the ISensorDataListener on the service
	 * 
	 * @param listener
	 *            the listener
	 */
	public void register(ISensorDataListener listener);

	/**
	 * unregister the ISensorDataListener from the service
	 * 
	 * @param listener
	 *            the listener
	 */
	public void unregister(ISensorDataListener listener);
}
