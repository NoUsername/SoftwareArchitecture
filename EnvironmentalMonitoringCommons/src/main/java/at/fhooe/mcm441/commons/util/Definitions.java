package at.fhooe.mcm441.commons.util;

public class Definitions {
	// =========================================
	// PREFERENCES
	// =========================================
	public static final String PREFERENCES_FILE = "EnvironmentalMonitoringServer.conf";

	// =========================================
	// XML
	// =========================================
	/**
	 * the xml entry string for the sensors
	 */
	public static final String XML_TAG_SENSORS = "Sensors";
	/**
	 * the xml entry string for the server
	 */
	public static final String XML_TAG_SERVER = "Server";
	/**
	 * the xml attribute name for the value
	 */
	public static final String XML_ATTR_VALUE = "value";

	// =========================================
	// PREFIXES
	// =========================================
	/**
	 * the prefix for the sensors
	 */
	public static final String PREFIX_SENSORS = "sensors";
	/**
	 * the prefix for the server (maybe this is not required, maybe it is...)
	 */
	public static final String PREFIX_SERVER = "server";
	/**
	 * the prefix seperator that seperates the prefixes
	 */
	public static final String PREFIX_SEPERATOR = ".";
	/**
	 * the prefix for the sensor visibility
	 */
	public static final String PREFIX_SENSORS_VISIBILITY = PREFIX_SENSORS
			+ PREFIX_SEPERATOR + "visibility";
	/**
	 * the prefix for the sensor polling time
	 */
	public static final String PREFIX_SENSORS_POLLTIME = PREFIX_SENSORS
			+ PREFIX_SEPERATOR + "polltime";
	/**
	 * the prefix for the server port
	 */
	public static final String PREFIX_SERVER_PORT = PREFIX_SERVER
			+ PREFIX_SEPERATOR + "port";

	/**
	 * the prefix for the server port for the sensors
	 */
	public static final String PREFIX_SERVER_SENSORS_PORT = PREFIX_SERVER
			+ PREFIX_SEPERATOR + "sensorPort";

	/**
	 * the prefix for the server port for the sensors
	 */
	public static final String PREFIX_SERVER_DEFAULT_POLLING = PREFIX_SERVER
			+ PREFIX_SEPERATOR + "polltime";

	/**
	 * the prefix for the default number of sensor data till a new file is
	 * created
	 */
	public static final String PREFIX_SERVER_NUMBER_OF_SENSORDATA = PREFIX_SERVER
			+ PREFIX_SEPERATOR + "numberofsensordata";

	/**
	 * the prefix for the html output path for the html files
	 */
	public static final String PREFIX_SERVER_OUTPUT_PATH_HTML = PREFIX_SERVER
			+ PREFIX_SEPERATOR + "htmloutputpath";

	/**
	 * the prefix for the normal log output path for the log files
	 */
	public static final String PREFIX_SERVER_OUTPUT_PATH_NORMAL_LOG = PREFIX_SERVER
			+ PREFIX_SEPERATOR + "normallogoutputpath";

	/**
	 * the prefix for the date format output string for the html files
	 */
	public static final String PREFIX_SERVER_DATE_FORMAT = PREFIX_SERVER
			+ PREFIX_SEPERATOR + "filedateformat";
}
