package at.fhooe.mcm441.server.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import at.fhooe.mcm441.sensor.Sensor;
import at.fhooe.mcm441.server.Server;
import at.fhooe.mcm441.server.utility.Definitions;

/**
 * the output class which exports the sensor data into a log file specified in
 * the Preferences
 * 
 * @author Manuel Lachberger
 * 
 */
public class FileLogOutput extends Output {

	/**
	 * the file that defines the temp file that is required for the temporary
	 * data storage
	 */
	private File m_file = null;
	/**
	 * the file writer for the temp file where we store the temp data
	 */
	private FileWriter m_fileWriter = null;

	/**
	 * default constructor
	 */
	public FileLogOutput() {
		init();
		log.info("fileLog output started");
	}

	@Override
	public void onSensorDataReceived(Sensor sensor) {
		if (isExportToFile(sensor)) {
			String path = Server.getPreferences().getValue(
					Definitions.PREFIX_SERVER_OUTPUT_PATH_NORMAL_LOG)
					+ "/" + sensor.ident;

			File temp = new File(path);
			if (!temp.exists()) {
				if (!temp.isDirectory()) {
					if (!temp.mkdir()) {
						log.warn("couldn't create the file output log");
					}
				}
			}

			m_file = new File(getCorrectFileName(path) + ".log");
			try {
				if (!m_file.exists()) {
					if (!m_file.createNewFile()) {
						log.error("couldn't create the output file!");
						return;
					}
				}
				m_fileWriter = new FileWriter(m_file);
				if (m_file.canWrite()) {
					// create header and start table
					m_fileWriter.append("========================== \n");
					m_fileWriter.append("Sensor Data for SensorID: "
							+ sensor.ident + "\n");
					m_fileWriter.append("========================== \n\n");

					// we have to create a new file now

					for (Sensor s : m_sensors.get(sensor.ident)) {

						m_fileWriter.append(this.formatSensorData(s) + "\n");
					}
					exportToFile(null, null);
				}
			} catch (IOException e) {
				log.error("couldn't export the file!", e);
			}
		}
	}

	@Override
	protected void finish() {
		try {
			m_fileWriter.flush();
			m_fileWriter.close();
		} catch (IOException e) {
			log.error("couldnt create the log file", e);
		}
	}

	@Override
	protected void exportToFile(String path, Sensor sensor) throws IOException {

		// write to file
		finish();

		// we are done exporting -> create new object
		init();
	}

	@Override
	protected void init() {
		m_sensors.clear();
	}
}
