package at.fhooe.mcm441.server.clients;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import at.fhooe.mcm441.commons.util.IPoolObserver;
import at.fhooe.mcm441.commons.util.ITimeout;
import at.fhooe.mcm441.commons.util.PooledExecutor;

/**
 * an improved version of the ClientAbstraction that uses
 * a thread-pool with timeouts and limits on the wait-queue to
 * do the sending to the clients 
 * 
 * @author Paul Klingelhuber
 *
 */
public class ClientAbstractionPooled  extends ClientAbstraction implements IPoolObserver {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this
			.getClass().getName());
	
	private PooledExecutor executor;
	
	public ClientAbstractionPooled() {
		executor = new PooledExecutor(50, 300, 200, this);
	}
	
	protected void broadcastToAllClients(final String msg) {
		List<ServerClient> all = null;
		synchronized (m_clients) {
			// copy the entries to be reistent to modifications while sending
			List<ServerClient>targets = m_clients.getAllClients();
			all = new ArrayList<ServerClient>(targets);
		}
		
		log.info("sending to all clients " + all.size());
		
		// if a timeout is reached for a client, we disconnect from it:
		for (final ServerClient sc : all) {
			Runnable r = new Runnable() {
				@Override
				public void run() {	
					if (!sc.getClientConnection().sendMessage(msg)) {
						onClientDisconnectes(sc.getClientInfo());
					}
				}
			};
			executor.execute(r, -1, new ITimeout() {
				@Override
				public void timedOut() {
					onClientDisconnectes(sc.getClientInfo());
				}
			});
		}
	}
	
	@Override
	protected void sendToClients(List<ServerClient> clients, final String msg) {
		
		for (final ServerClient sc : clients) {
			Runnable r = new Runnable() {
				@Override
				public void run() {	
					if (!sc.getClientConnection().sendMessage(msg)) {
						onClientDisconnectes(sc.getClientInfo());
					}
				}
			};
			
			executor.execute(r, -1, new ITimeout() {
				@Override
				public void timedOut() {
					onClientDisconnectes(sc.getClientInfo());
				}
			});
		}
	}

	@Override
	public void onQueueLimitReached(int size) {
		log.warn("we reached the limit on our thread-pool-wait-queue: " + size);
	}

}
