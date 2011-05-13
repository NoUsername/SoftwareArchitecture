package at.fhooe.mcm441.sensor;

import org.slf4j.Logger;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	Logger log = org.slf4j.LoggerFactory.getLogger("Main");
		log.trace("test");
		log.debug("test");
		log.info("test");
		log.warn("test");
		log.error("test");
		
	}
	
	
}
