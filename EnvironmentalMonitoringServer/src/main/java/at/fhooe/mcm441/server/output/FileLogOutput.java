package at.fhooe.mcm441.server.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;

import at.fhooe.mcm441.sensor.Sensor;
import at.fhooe.mcm441.server.Server;
import at.fhooe.mcm441.server.utility.Definitions;

/**
 * the output class which exports the sensor data into a log file specified in
 * the Preferences
 * 
 * TODO: maybe think about creating an own folder for each sensor id
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
		if (sensor != null && m_file != null && m_fileWriter != null
				&& m_file.canWrite()) {
			// valid
			try {
				// this is the first sensor value -> create header and start
				// table,
				// now that we have the sensor data
				if (m_sensorCount == 0) {
					// add header

					m_fileWriter.append("========================== \n");
					m_fileWriter.append("Sensor Data for SensorID: "
							+ sensor.ident + "\n");
					m_fileWriter.append("========================== \n\n");
				}

				String numberOfSensors = Server.getPreferences().getValue(
						Definitions.PREFIX_SERVER_NUMBER_OF_SENSORDATA);
				int num = 0;
				try {
					num = Integer.parseInt(numberOfSensors);
				} catch (NumberFormatException ex) {
					log.error(
							"not a valid number stored under PREFIX_SERVER_NUMBER_OF_SENSORDATA",
							ex);
					return;
				}

				if (m_sensorCount >= num) {
					// we have to create a new file now
					try {
						exportToFile(
								Server.getPreferences()
										.getValue(
												Definitions.PREFIX_SERVER_OUTPUT_PATH_NORMAL_LOG),
								sensor);
					} catch (IOException e) {
						log.error("couldn't export the file!", e);
					}
					// we have to reset the counter
					m_sensorCount = 0;
				}

				m_fileWriter.append(this.formatSensorData(sensor) + "\n");

				m_sensorCount++;
			} catch (IOException e) {
				log.error("can't write to file", e);
			}
		} else {
			log.warn("couldn't write sensor data in temp file!");
		}
	}

	@Override
	protected void finish() {
		try {
			m_fileWriter.flush();
			m_fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void init() {
		try {
			m_file = new File("tempFile.log");
			if (m_file.exists()) {
				if (!m_file.delete()) {
					log.error("couldn't reset the temporary file");
				}
			}

			// file deleted or doesn't exist -> now cretae it
			if (!m_file.createNewFile()) {
				log.error("couldn't create the temp file!");
			}

			m_fileWriter = new FileWriter(m_file);
		} catch (IOException e) {
			log.warn("couldn't create the correct filepath", e);
		}

	}

	@Override
	protected void exportToFile(String path, Sensor sensor) throws IOException {
		if (path != null && m_file != null && m_fileWriter != null) {

			finish();

			String filePath = (getCorrectFileName(path) + ".log");
			if (filePath != null) {
				// filePath.replace("\\", "\\");
				File file = new File(filePath);
				if (file.createNewFile()) {
					copyFile(m_file, file);
				} else {
					log.error("couldn't create the log file!");
				}
			}
		} else {
			// error occured
			log.warn("file writer is null or the path is invalid!");
		}
		// we are done exporting -> create new object
		init();

	}

	/**
	 * copy the file contents to another location
	 * 
	 * @param in
	 *            the file you want to copy
	 * @param out
	 *            the new destination
	 * @throws IOException
	 */
	private static void copyFile(File in, File out) throws IOException {
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (IOException e) {
			throw e;
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}
}
