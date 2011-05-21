package at.fhooe.mcm441.server.clients;

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
	
	private static final int POOL_THREADS = 50;
	private static final int WAIT_QUEUE_SIZE_WARNING = 500;
	/** no task executed by the pool is allowed to take longer than that much milliseconds */
	private static final int MAX_EXECUTION_TIME = 200;
	
	private PooledExecutor executor;
	
	public ClientAbstractionPooled() {
		executor = new PooledExecutor(POOL_THREADS, WAIT_QUEUE_SIZE_WARNING, MAX_EXECUTION_TIME, this);
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

	
	long lastWarnTime = 0;
	@Override
	public void onQueueLimitReached(int size) {
		long now = System.currentTimeMillis();
		if (now - lastWarnTime > 2000) {
			// don't flood log
			lastWarnTime = now;
			log.warn("we reached the limit on our thread-pool-wait-queue: " + size);
		}
	}

}
