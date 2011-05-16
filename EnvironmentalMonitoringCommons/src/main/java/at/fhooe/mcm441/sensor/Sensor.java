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
		return String.format("Sensor id={0} polling={1}, value={2}, valueType={3}, description={4}", ident, isPolling, data, dataType, description);
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
