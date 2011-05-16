package at.fhooe.mcm441.commons.network;

import java.io.InputStream;
import java.net.ServerSocket;

import org.slf4j.Logger;

public class NetworkService extends NetworkServiceClient {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());

	private ServerSocket sock = null;
	
	public NetworkService(IPackageListener update) {
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
		
		m_isRunning = true;
		m_thread = new Thread(new Runnable() {
			@Override
			public void run() {
				startServer(port);
			}
		});
		m_thread.start();
		return true;
	}
	
	public void stop() {
		super.stop();
		NWUtils.tryClose(sock);
	}
	
	private void startServer(int port) {
		InputStream is;
		while (m_isRunning) {
			is = null;
			try {
				m_curSock = sock.accept();
				is = m_curSock.getInputStream();
				
				do {
					long l = readPackageSize(is);
					
					String msg = readText(is, l);
					m_update.onNewPackage(msg);
					
				} while (m_isRunning);
			} catch (Exception _e) {
				log.warn("problem with client");
				NWUtils.tryClose(is);
			}
			m_curSock = null;
		}
	}
	

}
