package at.fhooe.mcm441.server.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.w3c.jigsaw.html.HtmlGenerator;

import at.fhooe.mcm441.sensor.Sensor;
import at.fhooe.mcm441.server.Server;
import at.fhooe.mcm441.server.utility.Definitions;

/**
 * the output class which exports the sensor data into an html file specified in
 * the Preferences
 * 
 * TODO: maybe think about creating an own folder for each sensor id
 * 
 * @author Manuel Lachberger
 * 
 */
public class HtmlOutput extends Output {
	/**
	 * the html generator object
	 */
	private HtmlGenerator m_htmlGenerator = null;

	/**
	 * constuctor
	 * 
	 * @throws IOException
	 */
	public HtmlOutput() {
		init();
		log.info("html output started");
	}

	@Override
	protected void init() {
		// create new object with the specified title
		m_htmlGenerator = new HtmlGenerator("Sensor Output");

		// insert the javascript code to get the correct links
		addLinksForPage();

	}

	@Override
	protected void finish() {
		// finish the table
		m_htmlGenerator.append("</table>");

		m_htmlGenerator.append("<div id=\"links\">");
		m_htmlGenerator.append("</div>");

	}

	/**
	 * exports the m_htmlGenerator to the specified file
	 * 
	 * @param path
	 *            the path and filename to the file
	 * @throws IOException
	 */
	protected void exportToFile(String path, Sensor sensor) throws IOException {

		if (path != null && m_htmlGenerator != null) {

			finish();

			// get the html code as byte[]
			InputStream stream = m_htmlGenerator.getInputStream();
			int bytesAvailable = stream.available();
			byte[] temp = new byte[bytesAvailable];

			StringBuffer stringBuffer = new StringBuffer();
			// most of the time this while loop will only be executed once
			while (bytesAvailable > 0) {
				int bytesRead = stream.read(temp, 0, bytesAvailable);
				if (bytesRead > 0) {
					// valid
					stringBuffer.append(new String(temp));
					// System.out.println(htmlCode);
					bytesAvailable = stream.available();
				}
			}
			if (stringBuffer != null && stringBuffer.length() > 0) {
				// create the file and write it there
				String filePath = (getCorrectFileName(path) + ".htm");
				if (filePath != null) {
					// filePath.replace("\\", "\\");
					File file = new File(filePath);
					if (file.createNewFile()) {
						FileWriter writer = new FileWriter(file);
						writer.write(stringBuffer.toString());
						writer.flush();
						writer.close();
					} else {
						log.error("couldn't create the html file!");
					}
				}
			} else {
				log.warn("couldn't write the file because the stringbuffer is empty!");
			}
		} else {
			// error occured
			log.warn("m_htmlGenerator is null or the path is invalid!");
		}
		// we are done exporting -> create new object
		init();

	}

	/**
	 * adds the javascript source code that has to be packed in every html page
	 * that the links work in there
	 */
	private void addLinksForPage() {
		m_htmlGenerator.append("<script src=\"jq.js\"></script>");
		m_htmlGenerator.append("<script>");
		m_htmlGenerator.append("function callback(data, textStatus, jqXHR) {");
		m_htmlGenerator.append("$('#links').html(data);");
		m_htmlGenerator.append("}");
		m_htmlGenerator.append("function reloadLinks() {");
		m_htmlGenerator
				.append("jQuery.get(\"./linkprovider.php\", \"\", callback, \"html\");");
		m_htmlGenerator.append("setTimeout(reloadLinks, 5000);");
		m_htmlGenerator.append("}");
		m_htmlGenerator.append("$(document).ready(function(){");
		m_htmlGenerator.append("setTimeout(reloadLinks, 5);" + "});");
		m_htmlGenerator.append("</script>");
	}

	@Override
	public void onSensorDataReceived(Sensor sensor) {
		if (m_htmlGenerator != null && sensor != null) {

			// this is the first sensor value -> create header and start table,
			// now that we have the sensor data
			if (m_sensorCount == 0) {
				// add header
				m_htmlGenerator.append("<h1>Sensor Data for SensorID: "
						+ sensor.ident + "</h1>");

				// add line break
				m_htmlGenerator.append("<br />");

				// start the table for the sensor values
				m_htmlGenerator.append("<table border=1>");
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
							Server.getPreferences().getValue(
									Definitions.PREFIX_SERVER_OUTPUT_PATH_HTML),
							sensor);
				} catch (IOException e) {
					log.error("couldn't export the file!", e);
				}
				// we have to reset the counter
				m_sensorCount = 0;
			}

			m_htmlGenerator.append(this.formatSensorData(sensor));

			m_sensorCount++;
		}
	}

	/**
	 * additionally gives the table html-view of it (tr & th)
	 */
	@Override
	protected String formatSensorData(Sensor sensor) {
		if (sensor != null) {
			return ("<tr><th>" + super.formatSensorData(sensor) + "</th></tr>");
		}
		return null;
	}
}
