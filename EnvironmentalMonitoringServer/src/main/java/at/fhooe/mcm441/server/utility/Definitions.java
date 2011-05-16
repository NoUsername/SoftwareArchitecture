package at.fhooe.mcm441.server.utility;

public class Definitions {
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
	 * the prefix for the server port
	 */
	public static final String PREFIX_SERVER_PORT = PREFIX_SERVER
			+ PREFIX_SEPERATOR + "port";
}
