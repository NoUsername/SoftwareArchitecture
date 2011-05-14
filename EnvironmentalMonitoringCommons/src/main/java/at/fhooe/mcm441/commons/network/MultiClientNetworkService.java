package at.fhooe.mcm441.commons.network;

import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;


public class MultiClientNetworkService {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());

	private ServerSocket m_sock = null;
	private IMultiClientNetworkListener m_update = null;
	private boolean m_isRunning = false;
	protected Thread m_thread = null;
	private IMultiClientNetworkEventsListener m_eventListener = null;
	
	public MultiClientNetworkService(IMultiClientNetworkListener update, IMultiClientNetworkEventsListener events) {
		m_update = update;
		m_eventListener = events;
	}
	
	public boolean isRunning() {
		return m_isRunning || (m_thread != null && m_thread.isAlive());
	}
	
	public boolean startListening(final int port) {
		if (isRunning()) {
			log.warn("cannot start listening, server already running");
			return false;
		}
		
		m_sock = null;
		try {
			m_sock = new ServerSocket(port);
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
		try {
			m_sock.close();
		} catch (Exception e) {
			log.warn("error while trying to stop.... ", e);
		}
	}
	
	private void startServer(int port) {
		Socket curSock = null;
		while (m_isRunning) {
			try {
				curSock = m_sock.accept();
				final Client clientData = new Client(curSock.getRemoteSocketAddress().toString());
				NetworkServiceClient realClient = new NetworkServiceClient(curSock, new IPackageListener() {
					@Override
					public void onNewPackage(final String newPackage) {
						if (m_update != null)
							m_update.onNewPackage(clientData, newPackage);
					}
				},
				new IConnectionStatusListener() {
					@Override
					public void onConnectionLost() {
						if (m_eventListener != null)
							m_eventListener.onClientDisconnectes(clientData);
					}
					@Override
					public void onConnectionEstablished() {
						// ignored
					}
				});
				m_eventListener.onNewClient(clientData, realClient);
				// call the special package-private method that starts listening
				// on the configured socket
				realClient.connectToCurrentSocket();
				
			} catch (Exception e1) {
				log.warn("problem with client", e1);
				try {
					if (curSock != null)
						curSock.close();
				} catch (Exception e2) {
					// ignored on purpose
				}
			}
			curSock = null;
		}
	}

}
