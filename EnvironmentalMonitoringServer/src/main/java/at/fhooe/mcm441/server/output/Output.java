package at.fhooe.mcm441.server.output;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
public abstract class Output implements ISensorDataListener {
	// no additional public functions are required
	protected final Logger log = org.slf4j.LoggerFactory.getLogger(this
			.getClass().getName());

	/**
	 * counts the number of sensor data that was received and should be added to
	 * the output. if this reaches a certain threshold a new file should be
	 * generated and the counter be reset
	 */
	protected int m_sensorCount = 0;

	/**
	 * finish the file
	 */
	protected abstract void finish();

	/**
	 * start a new file
	 */
	protected abstract void init();

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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				log.warn("path parameter is wrong");
			}
		}
		return null;
	}
}
