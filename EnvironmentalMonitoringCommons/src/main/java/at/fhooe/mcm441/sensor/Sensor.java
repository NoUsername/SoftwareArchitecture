package at.fhooe.mcm441.sensor;

/**
 * could be used inside the sensor program as well, but also in the server as
 * data-structure
 * 
 * @author manuel
 * 
 */
public abstract class Sensor {

	/**
	 * the identifier of the sensor
	 */
	public String ident = null;

	/**
	 * the description string
	 */
	public String description = null;

	/**
	 * polling sensor?
	 */
	public boolean isPolling = false;

	/**
	 * the data value
	 */
	public double data = 0;

	/**
	 * meter, celsius, ...
	 */
	public String dataType = null;
}
