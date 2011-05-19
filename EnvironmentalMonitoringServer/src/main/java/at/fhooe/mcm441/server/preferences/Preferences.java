package at.fhooe.mcm441.server.preferences;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;

import at.fhooe.mcm441.server.utility.Definitions;

/**
 * handles all the preferences and notifies the registered listener in case of
 * updates
 * 
 * @author Manuel Lachberger
 * 
 */
public class Preferences implements INotificationService, IPreferencenWriter,
		IPreferenceReader {

	/**
	 * the logger instance that is used for logging
	 */
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this
			.getClass().getName());
	/**
	 * all the prefixes that start with this string are persistent, default =
	 * "server"
	 */
	public static final String PERSISTENT_PREFIX_GROUP = Definitions.PREFIX_SERVER;
	/**
	 * the persistent storage that handles the read and write operations for the
	 * preferences file
	 */
	private PreferencePersistentStorage m_persistentStorage = null;
	/**
	 * hashtable that stores the listeners according to the prefixes.
	 */
	private Map<String, Vector<IChangeListener>> m_listeners = null;
	/**
	 * stores the prefix-value pairs for the confguration (e.g. sensor.port =
	 * 4444, sensor.visibility.id5 = true)
	 */
	private Map<String, String> m_configuration = null;

	/**
	 * default constructor
	 */
	public Preferences() {
		m_listeners = new Hashtable<String, Vector<IChangeListener>>();
		m_configuration = new Hashtable<String, String>();

		try {
			m_persistentStorage = new PreferencePersistentStorage(
					Definitions.PREFERENCES_FILE);
		} catch (FileNotFoundException e) {
			log.error("Preferences::Constructor couldn't find the preferences "
					+ "file --> this shouldn't happen - Exception: ", e);
		} catch (IOException e) {
			log.error(
					"Preferences::Constructor couldn't write the preferences "
							+ "file - Exception: ", e);
		}
	}

	@Override
	public boolean register(String prefix, IChangeListener listener) {
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
		return false;
	}

	@Override
	public boolean unregister(String prefix, IChangeListener listener) {
		if (m_listeners != null) {
			if (prefix != null && listener != null) {
				if (m_listeners.containsKey(prefix)) {
					if (m_listeners.get(prefix).contains(listener)) {
						return m_listeners.get(prefix).remove(listener);
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean addNewPreference(String prefix, String value) {
		if (m_configuration != null && prefix != null && value != null
				&& prefix.length() > 0 && value.length() > 0) {
			if (!storePersistent(prefix, value)) {
				// store just temporary -> not persistent
				if (m_configuration.containsKey(prefix)) {
					log.info("Preferences::addNewPreference preferences"
							+ " already contains key \"" + prefix
							+ "\" value was overriten");
				}
				m_configuration.put(prefix, value);

				// now we have to inform the listeners about a change in the
				// values
				informListeners(prefix, value);
				return true;
			} else {
				// persistent
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean updatePreference(String prefix, String value) {
		if (m_configuration != null && prefix != null && value != null
				&& prefix.length() > 0) {
			if (!storePersistent(prefix, value)) {
				// store just temporary -> not persistent
				if (m_configuration.containsKey(prefix)) {
					// valid!
					String oldValue = m_configuration.get(prefix);

					log.info("Preferences::updatePreferences value overritten for prefix: "
							+ prefix
							+ " oldValue: "
							+ oldValue
							+ " newValue: "
							+ value);

					m_configuration.put(prefix, value);

					// now we have to inform the listeners about a change in the
					// values
					informListeners(prefix, value);

					return true;
				}
			} else {
				// persistent
				return true;
			}
		}
		return false;
	}

	@Override
	public Vector<String> getAllPrefixes() {
		if (m_configuration != null && m_configuration.size() > 0) {
			Vector<String> result = new Vector<String>();
			Collection<String> set = m_configuration.keySet();
			if (set != null) {
				for (String s : set) {
					if (s != null) {
						result.add(s);
					}
				}
				return result;
			}
		}
		return null;
	}

	@Override
	public String getValue(String prefix) {
		if (m_configuration != null && prefix != null && prefix.length() > 0) {
			String persistent = getPersistent(prefix);
			if (persistent == null) {
				// not a persistent one
				if (m_configuration.containsKey(prefix)) {
					return m_configuration.get(prefix);
				}
			} else {
				// that's a persistent one
				return persistent;
			}
		}
		return null;
	}

	/**
	 * checks if the given prefix with the value has to be stored in persistent
	 * storage
	 * 
	 * @param prefix
	 *            the prefix
	 * @param value
	 *            the value for the prefix
	 * @return true if it's a persistent storage, false otherwise
	 */
	private boolean storePersistent(String prefix, String value) {
		if (m_persistentStorage != null && prefix != null && value != null) {
			if (prefix.startsWith(PERSISTENT_PREFIX_GROUP)) {
				// yep, that's the one we have to save in persistent storage
				return m_persistentStorage.writeProperty(prefix, value);
			}
		}
		return false;
	}

	/**
	 * checks if the prefix is a persistent one and returns the correct value if
	 * it is stored in the persistent storage, returns null in case of error
	 * 
	 * @param prefix
	 *            the prefix you want to know the value of
	 * @return the persistent preference as string, null in case of error
	 */
	private String getPersistent(String prefix) {
		if (prefix != null && prefix.length() > 0
				&& m_persistentStorage != null) {
			if (prefix.startsWith(PERSISTENT_PREFIX_GROUP)) {
				// yep thats a persistent one
				return m_persistentStorage.readProperty(prefix);
			}
		}
		return null;
	}

	/**
	 * get all the subscribed listeners by the prefix
	 * 
	 * TODO: make this function faster/better -> not the optimal solution, but
	 * for first version quite okay
	 * 
	 * @param prefix
	 *            the prefix you want to have the listeners from
	 * @return a vector of listeners
	 */
	public Vector<IChangeListener> getListenersByPrefix(String prefix) {
		Vector<IChangeListener> result = null;
		if (m_listeners != null && m_listeners.size() > 0 && prefix != null
				&& prefix.length() > 0) {
			Collection<String> set = m_listeners.keySet();
			if (set != null) {
				for (String s : set) {
					if (s != null) {
						if (prefix.contains(s)) {
							// found
							result = m_listeners.get(s);
							// TODO: try if the break is required here or not:
							// unit tests
							// break;
						}
					}
				}
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
		if (prefix != null && value != null && prefix.length() > 0) {
			if (m_configuration == null
					|| !m_configuration.get(prefix).equals(value)) {
				log.warn("Preferences::informListeners there was an error when "
						+ "trying to save the preferences -> given value is not "
						+ "the same as the one stored in preferenecs!");
				// TODO: think about this return --> is it useful
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