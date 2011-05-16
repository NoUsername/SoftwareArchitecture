package at.fhooe.mcm441.commons.network;

import java.io.Closeable;
import java.net.ServerSocket;
import java.net.Socket;

public class NWUtils {
private NWUtils() {}

	/* **
	 * utility methods
	 */
	
	public static void tryClose(Closeable c) {
		if (c ==null)
			return;
		
		try {
			c.close();
		} catch (Exception e) {
			// ignored here
		}
	}
	
	public static void tryClose(Socket c) {
		if (c ==null)
			return;
		
		try {
			c.close();
		} catch (Exception e) {
			// ignored here
		}
	}
	
	public static void tryClose(ServerSocket c) {
		if (c ==null)
			return;
		
		try {
			c.close();
		} catch (Exception e) {
			// ignored here
		}
	}
	
	
}
