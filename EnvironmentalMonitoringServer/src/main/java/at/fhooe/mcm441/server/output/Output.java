package at.fhooe.mcm441.server.output;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import at.fhooe.mcm441.sensor.Sensor;
import at.fhooe.mcm441.server.Server;
import at.fhooe.mcm441.server.processing.ISensorDataListener;
import at.fhooe.mcm441.server.utility.Definitions;

/**
 * defines the output class that is implemented by all the possible outputs.
 * currently only the ISensorDataListener functions are required and no further
 * functions
 * 
 * @author Manuel Lachberger
 * 
 */
public abstract class Output implements ISensorDataListener, Runnable {

	protected List<Runnable> m_runnableList = null;

	protected Thread m_thread = null;

	// no additional public functions are required
	protected final Logger log = org.slf4j.LoggerFactory.getLogger(this
			.getClass().getName());

	/**
	 * the sensor list
	 */
	protected Map<String, List<Sensor>> m_sensors = null;

	/**
	 * finish the file
	 */
	protected abstract void finish();

	/**
	 * start a new file
	 */
	protected abstract void init();

	public Output() {
		m_sensors = new Hashtable<String, List<Sensor>>();
		m_runnableList = new ArrayList<Runnable>();
		m_thread = new Thread(this);
		m_thread.start();
	}

	/**
	 * adds a new sensor value to the list and return true if a new file should
	 * be extracted
	 * 
	 * @param sensor
	 *            the sensor that should be added
	 */
	synchronized protected boolean isExportToFile(Sensor sensor) {
		if (m_sensors != null && sensor != null) {
			Sensor s = sensor.clone();
			if (m_sensors.containsKey(s.ident)) {
				List<Sensor> tempList = m_sensors.get(s.ident);
				tempList.add(s);
				m_sensors.put(s.ident, tempList);
			} else {
				List<Sensor> temp = new ArrayList<Sensor>();
				temp.add(s);
				m_sensors.put(s.ident, temp);
			}
			int num = 0;
			String numberOfSensors = Server.getPreferences().getValue(
					Definitions.PREFIX_SERVER_NUMBER_OF_SENSORDATA);
			try {
				num = Integer.parseInt(numberOfSensors);
			} catch (NumberFormatException ex) {
				log.error(
						"not a valid number stored under PREFIX_SERVER_NUMBER_OF_SENSORDATA",
						ex);
				return false;
			}
			int size = m_sensors.get(s.ident).size();
			if (size >= num) {
				return true;
			}
		}
		return false;
	}

	/**
	 * formats the sensor data in a correct way
	 * 
	 * @param sensor
	 *            the input sensor data
	 * @return the beautifully formatted sensor data, or null in case of error
	 */
	protected String formatSensorData(Sensor sensor) {
		if (sensor != null) {
			return (sensor.toString());
		}
		return null;
	}

	/**
	 * exports the file to the specified path
	 * 
	 * @param path
	 *            the path for the file
	 * @param sensor
	 *            the last sensor data
	 * @throws IOException
	 */
	protected abstract void exportToFile(String path, Sensor sensor)
			throws IOException;

	/**
	 * gets the new file name and path for the specified sensor
	 * 
	 * @param path
	 *            the path to the parent folder for the output
	 * @param id
	 *            the id of the sensor object which is required for the folder
	 *            naming
	 * @return the path with filename for the output (file extension must still
	 *         be added)
	 */
	protected String getCorrectFileName(String path) {
		if (path != null) {
			File parentFilePath = new File(path);

			parentFilePath = parentFilePath.getAbsoluteFile();

			if (!parentFilePath.exists()) {
				if (!parentFilePath.mkdir()) {
					log.error("couldn't create the main dir: " + path);
					return null;
				}
			}
			if (parentFilePath.isDirectory()) {
				// correct path given
				File childPath = new File(path);

				childPath = childPath.getAbsoluteFile();

				if (!childPath.exists()) {
					// create the directory
					if (!childPath.mkdir()) {
						log.warn("couldn't create directory");
						return null;
					}
				}
				Date dateTime = new Date(System.currentTimeMillis());

				SimpleDateFormat format = new SimpleDateFormat(Server
						.getPreferences().getValue(
								Definitions.PREFIX_SERVER_DATE_FORMAT));

				String fileName = format.format(dateTime);

				try {
					return new File(childPath, fileName).getCanonicalPath();
				} catch (IOException e) {
					log.error("couldn't get the correct filepath", e);
				}
			} else {
				log.warn("path parameter is wrong");
			}
		}
		return null;
	}

	@Override
	public void run() {
		while (true) {
			if (m_runnableList != null && m_runnableList.size() > 0) {
				m_runnableList.get(0).run();
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.error("couldn't sleep", e);
			}
		}

	}
}
