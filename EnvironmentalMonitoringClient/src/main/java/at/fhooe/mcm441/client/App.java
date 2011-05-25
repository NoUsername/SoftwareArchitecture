package at.fhooe.mcm441.client;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception {
    	if (args.length > 0) {
    		SensorManager.main(args);
    	} else {
    		SensorViewer.main(args);
    	}
	}
	
}
