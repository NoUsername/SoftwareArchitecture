package at.fhooe.mcm441.server.output;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.w3c.jigsaw.html.HtmlGenerator;

import at.fhooe.mcm441.commons.util.Util;
import at.fhooe.mcm441.sensor.Sensor;
import at.fhooe.mcm441.server.Server;
import at.fhooe.mcm441.server.utility.Definitions;

/**
 * the output class which exports the sensor data into an html file specified in
 * the Preferences
 * 
 * @author Manuel Lachberger
 * 
 */
public class HtmlOutput extends Output {

	class HtmlFileExporter implements Runnable {

		private Sensor m_sensor = null;

		public HtmlFileExporter(Sensor s) {
			m_sensor = s;
		}

		@Override
		public void run() {
			if (m_htmlGenerator != null && m_sensor != null) {

				// create header and start table
				m_htmlGenerator.append("<h1>Sensor Data for SensorID: "
						+ m_sensor.ident + "</h1>");

				// add line break
				m_htmlGenerator.append("<br />");

				// start the table for the sensor values
				m_htmlGenerator.append("<table border=1>");

				// we have to create a new file now
				List<Sensor> sensors = m_sensors.get(m_sensor.ident);
				for (Sensor s : sensors) {
					m_htmlGenerator.append(formatSensorData(s));
				}

				try {
					exportToFile(
							Server.getPreferences().getValue(
									Definitions.PREFIX_SERVER_OUTPUT_PATH_HTML),
							m_sensor);
				} catch (IOException e) {
					log.error("couldn't export the file!", e);
				}
			}
			m_runnableList.remove(this);
		}
	}

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
	synchronized protected void exportToFile(String path, Sensor sensor)
			throws IOException {

		if (path != null && m_htmlGenerator != null) {

			String parentPath = path;
			path = path + "/" + sensor.ident;

			File f = new File(path);
			if (!f.exists() || !f.isDirectory()) {
				if (!f.mkdir()) {
					log.error("couldn't create the html output directory");
				} else {
					// could create the directory -> copy the files there
					copyLinkFiles(parentPath, path);
				}
			}

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
					if (!file.exists()) {
						if (!file.createNewFile()) {
							log.error("couldn't create the html file!");
						}
					}
					FileWriter writer = new FileWriter(file);
					writer.write(stringBuffer.toString());
					writer.flush();
					writer.close();
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

		m_sensors.get(sensor.ident).clear();

	}

	/**
	 * adds the javascript source code that has to be packed in every html page
	 * that the links work in there
	 */
	private void addLinksForPage() {
		m_htmlGenerator.append("<script src=\"./jq.js\"></script>");
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

	/**
	 * copies the link files to the child directory specified. the link files
	 * are stored in the parent path directory
	 * 
	 * @param parentPath
	 *            the path to the directory where the link-files are stored
	 * @param childPath
	 *            the path to the directory where the files should be copied to
	 */
	private static void copyLinkFiles(String parentPath, String childPath)
			throws IOException {
		if (parentPath != null && childPath != null) {
			File parent = new File(parentPath);
			File child = new File(childPath);

			if (parent.exists() && parent.isDirectory() && child.exists()
					&& child.isDirectory()) {

				FilenameFilter filter = new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".js") || name.endsWith(".php");
					}
				};
				// get the 2 link-files
				File[] children = parent.listFiles(filter);
				File out = null;

				for (File linkFile : children) {
					out = new File(childPath + "/" + linkFile.getName());
					Util.copyFile(linkFile, out);
				}
			}
		}
	}

	@Override
	public void onSensorDataReceived(Sensor sensor) {
		if (isExportToFile(sensor)) {
			// only create runnable when we have to write the file and not for
			// simple adding functions
			HtmlFileExporter ex = new HtmlFileExporter(sensor);
			m_runnableList.add(ex);
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
