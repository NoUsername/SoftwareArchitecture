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
	
	
	@Override
	protected void sendToClient(final ServerClient client, final String msg) {
		if (client == null)
			return;
		
		Runnable r = new Runnable() {
			@Override
			public void run() {	
				if (!client.getClientConnection().sendMessage(msg)) {
					onClientDisconnectes(client.getClientInfo());
				}
			}
		};
		
		executor.execute(r, -1, new ITimeout() {
			@Override
			public void timedOut() {
				onClientDisconnectes(client.getClientInfo());
			}
		});
	}

	@Override
	public void onQueueLimitReached(int size) {
		log.warn("we reached the limit on our thread-pool-wait-queue: " + size);
	}

}
