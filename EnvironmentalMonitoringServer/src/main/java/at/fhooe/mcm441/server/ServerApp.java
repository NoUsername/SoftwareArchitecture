package at.fhooe.mcm441.server;

import org.slf4j.Logger;

/**
 * Hello world!
 *
 */
public class ServerApp 
{
    public static void main( String[] args )
    {
    	Logger log = org.slf4j.LoggerFactory.getLogger("Main");
		log.info("starting server");
		
		Server.initServer();
	}
	
}
