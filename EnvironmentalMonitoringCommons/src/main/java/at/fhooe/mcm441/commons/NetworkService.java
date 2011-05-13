package at.fhooe.mcm441.commons;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ServerSocket;

import org.slf4j.Logger;

public class NetworkService extends NetworkServiceClient {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());

	private ServerSocket sock = null;
	
	public NetworkService(PackageListener update) {
		super(update);
	}
	
	public boolean startListening(final int port) {
		if (isRunning()) {
			log.warn("cannot start listening, server already running");
			return false;
		}
		
		sock = null;
		try {
			sock = new ServerSocket(port);
		} catch (Exception e) {
			log.warn("cannot start server at port " + port);
			return false;
		}
		
		isRunning = true;
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				startServer(port);
			}
		});
		thread.start();
		return true;
	}
	
	public void stop() {
		super.stop();
		try {
			sock.close();
		} catch (Exception e) {
			log.warn("error while trying to stop.... ", e);
		}
	}
	
	private void startServer(int port) {
		InputStream is;
		while (isRunning) {
			is = null;
			try {
				curSock = sock.accept();
				is = curSock.getInputStream();
				Reader r = new InputStreamReader( is );
				
				do {
					long l = readPackageSize(is);
					
					String msg = readText(r, l);
					update.onNewPackage(msg);
					
				} while (isRunning);
			} catch (Exception _e) {
				log.warn("problem with client");
				tryClose(is);
			}
			curSock = null;
		}
	}
	

}
