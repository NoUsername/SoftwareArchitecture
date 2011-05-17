package at.fhooe.mcm441.server.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;

/**
 * the handler for the persistent preference storage (handles read and write
 * operations)
 * 
 * @author Manuel Lachberger
 * 
 */
public class PreferencePersistentStorage {

	/**
	 * the logger instance that is used for logging
	 */
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this
			.getClass().getName());
	/**
	 * specifies the file where the properties are saved
	 */
	private File m_file = null;
	/**
	 * the properties
	 */
	private Properties m_properties = null;

	/**
	 * constructor that takes the path to the config file
	 * 
	 * @param path
	 *            the path to the config file
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public PreferencePersistentStorage(String path)
			throws FileNotFoundException, IOException {
		this(new File(path));
	}

	/**
	 * the constructor which takes a File object as input. this file represents
	 * the config file
	 * 
	 * @param file
	 *            the config file
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public PreferencePersistentStorage(File file) throws FileNotFoundException,
			IOException {
		if (file != null) {
			m_properties = new Properties();
			if (!file.exists()) {
				if (!file.createNewFile()) {
					log.error("PreferencePersistentStorage::Constructor couldn't "
							+ "create the config file!");
					return;
				}

			}
			// file exists
			m_file = file;
			m_properties.load(new FileInputStream(m_file));
		}
	}

	/**
	 * reads the requested property from the file
	 * 
	 * @param property
	 *            the property you want to read
	 * @return the value of the property as string
	 */
	public String readProperty(String property) {
		if (m_properties != null && property != null) {
			return m_properties.getProperty(property);
		}
		return null;
	}

	/**
	 * writes the property with the given name into the config file
	 * 
	 * @param property
	 *            the name of the property
	 * @param value
	 *            the value that belongs to the property
	 * @return true in case of success, false in case of error
	 */
	public boolean writeProperty(String property, String value) {
		if (m_properties != null && property != null && value != null) {
			// write
			m_properties.setProperty(property, value);

			// save properties after each write operation
			saveProperties();
			return true;
		}
		// invalid
		return false;
	}

	/**
	 * saves the properties in the file output
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void saveProperties() {
		if (m_file != null && m_properties != null) {
			try {
				m_properties
						.store(new FileOutputStream(m_file),
								"The properties file for our EnvironmentalMonitoringServer");
			} catch (FileNotFoundException e) {
				log.error("PreferencePersistentStorage::saveProperties file couldn't be found! Exception:");
				e.printStackTrace();
			} catch (IOException e) {
				log.error("PreferencePersistentStorage::saveProperties couldn't write to file! Exception:");
				e.printStackTrace();
			}
		}
	}

	/**
	 * get the number of properties inside the preferences
	 * 
	 * @return the size
	 */
	public int size() {
		if (m_properties != null) {
			return m_properties.size();
		}
		// error
		return -1;
	}
}
