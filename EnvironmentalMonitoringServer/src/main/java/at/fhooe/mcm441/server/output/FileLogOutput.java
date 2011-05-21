package at.fhooe.mcm441.server.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

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
	 * the runnable target that exports the logfile to the specified output path
	 * 
	 * @author Manuel Lachberger
	 * 
	 */
	class FileLogExporter implements Runnable {
		/**
		 * the sensor object that is required as parameter
		 */
		private Sensor m_sensor = null;

		/**
		 * constructor that sets the parameter
		 * 
		 * @param s
		 *            the sensor object
		 */
		public FileLogExporter(Sensor s) {
			m_sensor = s;
		}

		@Override
		public void run() {

			String path = Server.getPreferences().getValue(
					Definitions.PREFIX_SERVER_OUTPUT_PATH_NORMAL_LOG)
					+ "/" + m_sensor.ident;

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
							+ m_sensor.ident + "\n");
					m_fileWriter.append("========================== \n\n");

					// we have to create a new file now
					List<Sensor> sensors = m_sensors.get(m_sensor.ident);
					for (Sensor s : sensors) {
						m_fileWriter.append(formatSensorData(s) + "\n");
					}
					exportToFile(null, m_sensor);
				}
			} catch (IOException e) {
				log.error("couldn't export the file!", e);

			}
			m_runnableList.remove(this);
		}

	}

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
			// only create runnable when we have to write the file and not for
			// simple adding functions
			FileLogExporter ex = new FileLogExporter(sensor);
			m_runnableList.add(ex);
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
	synchronized protected void exportToFile(String path, Sensor sensor)
			throws IOException {

		// write to file
		finish();

		// we are done exporting -> create new object
		init();

		// done exporting -> clear list
		m_sensors.get(sensor.ident).clear();
	}

	@Override
	protected void init() {
		// TODO Auto-generated method stub

	}
}
