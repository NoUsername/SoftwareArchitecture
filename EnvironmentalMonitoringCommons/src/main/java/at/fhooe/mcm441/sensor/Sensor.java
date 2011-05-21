package at.fhooe.mcm441.sensor;

/**
 * could be used inside the sensor program as well, but also in the server as
 * data-structure
 * 
 * can also be used from client to hold information about each sensor
 * 
 * @author manuel
 * 
 */
public class Sensor {
	
	/**
	 * default constructor which doesn't init any fields
	 */
	public Sensor() {
		
	}

	public Sensor(String ident, String description, boolean isPolling,
			double data, String dataType) {
		super();
		this.ident = ident;
		this.description = description;
		this.isPolling = isPolling;
		this.data = data;
		this.dataType = dataType;
	}
	
	@Override
	public String toString() {
		return String.format("Sensor id=%s polling=%b, value=%f, valueType=%s, description=%s", ident, isPolling, data, dataType, description);
	}
	
	@Override
	public Sensor clone(){
		return new Sensor(ident,description,isPolling,data,dataType);
	}

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
