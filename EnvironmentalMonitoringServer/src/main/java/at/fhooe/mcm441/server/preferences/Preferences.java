package at.fhooe.mcm441.server.preferences;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * handles all the preferences and notifies the registered listener in case of
 * updates
 * 
 * TODO: use config file for static values, like server port or sthg like that
 * -> for that we have to set the correct prefix to access it later on
 * 
 * @author Manuel Lachberger
 * 
 */
public class Preferences implements INotificationService, IPreferencenWriter,
		IPreferenceReader {
	/** this should probably be read from some config file */
	public static int SERVERPORT = 4444;

	/**
	 * hashtable that stores the listeners according to the prefixes.
	 */
	private Hashtable<String, Vector<IChangeListener>> m_listeners = null;

	/**
	 * stores the prefix-value pairs for the confguration (e.g. sensor.port =
	 * 4444, sensor.visibility.id5 = true)
	 */
	private Hashtable<String, String> m_configuration = null;

	/**
	 * default constructor
	 */
	public Preferences() {
		m_listeners = new Hashtable<String, Vector<IChangeListener>>();
		m_configuration = new Hashtable<String, String>();
	}

	@Override
	public boolean register(String prefix, IChangeListener listener) {
		if (this != null) {
			if (m_listeners != null) {
				if (prefix != null && listener != null) {
					if (m_listeners.containsKey(prefix)) {
						// the prefix is already in the list -> add the listener
						// to the vector
						m_listeners.get(prefix).add(listener);
					} else {
						// specify new entry in the hashtable for the specified
						// prefix
						Vector<IChangeListener> vec = new Vector<IChangeListener>();
						vec.add(listener);
						m_listeners.put(prefix, vec);
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean unregister(String prefix, IChangeListener listener) {
		if (this != null) {
			if (m_listeners != null) {
				if (prefix != null && listener != null) {
					if (m_listeners.containsKey(prefix)) {
						if (m_listeners.get(prefix).contains(listener)) {
							return m_listeners.get(prefix).remove(listener);
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean addNewPreference(String prefix, String value) {
		if (this != null && m_configuration != null && prefix != null
				&& value != null && prefix.length() > 0 && value.length() > 0) {
			if (m_configuration.containsKey(prefix)) {
				// TODO: call logger to log that configuration was overwritten
				// because it already exists!
			}
			m_configuration.put(prefix, value);

			// now we have to inform the listeners about a change in the values
			informListeners(prefix, value);

			return true;
		}
		return false;
	}

	@Override
	public boolean updatePreference(String prefix, String value) {
		if (this != null && m_configuration != null && prefix != null
				&& value != null && prefix.length() > 0) {
			if (m_configuration.containsKey(prefix)) {
				// valid!
				String oldValue = m_configuration.get(prefix);
				// TODO: call logger and tell that vlaue was overritten
				m_configuration.put(prefix, value);

				// now we have to inform the listeners about a change in the
				// values
				informListeners(prefix, value);

				return true;
			}
		}
		return false;
	}

	@Override
	public Vector<String> getAllPrefixes() {
		if (this != null && m_configuration != null
				&& m_configuration.size() > 0) {
			Vector<String> result = new Vector<String>();
			Enumeration<String> enume = m_configuration.keys();
			if (enume != null) {
				while (enume.hasMoreElements()) {
					result.add(enume.nextElement());
				}
				return result;
			}
		}
		return null;
	}

	@Override
	public String getValue(String prefix) {
		if (this != null && m_configuration != null && prefix != null
				&& prefix.length() > 0) {
			if (m_configuration.containsKey(prefix)) {
				return m_configuration.get(prefix);
			}
		}
		return null;
	}

	/**
	 * get all the subscribed listeners by the prefix
	 * 
	 * @param prefix
	 *            the prefix you want to have the listeners from
	 * @return a vector of listeners
	 */
	public Vector<IChangeListener> getListenersByPrefix(String prefix) {
		Vector<IChangeListener> result = null;
		if (this != null && m_listeners != null && m_listeners.size() > 0
				&& prefix != null && prefix.length() > 0) {
			if (m_listeners.containsKey(prefix)) {
				result = m_listeners.get(prefix);
			}
		}
		return result;
	}

	/**
	 * inform the listeners that are subscribed for the prefix about a new value
	 * 
	 * @param prefix
	 *            the prefix you want to check for subscribed listeners
	 * @param value
	 *            the value for the listeners to inform
	 */
	private void informListeners(String prefix, String value) {
		if (this != null && prefix != null && value != null
				&& prefix.length() > 0) {
			if (m_configuration == null
					|| !m_configuration.get(prefix).equals(value)) {
				// TODO: call logger --> not the correct values saved in table!
				// return;
			}
			Vector<IChangeListener> listeners = getListenersByPrefix(prefix);
			if (listeners != null && listeners.size() > 0) {
				// valid
				for (IChangeListener l : listeners) {
					if (l != null) {
						l.update(prefix, value);
					}
				}
			}
		}
	}
}