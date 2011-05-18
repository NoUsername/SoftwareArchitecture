package at.fhooe.mcm441.commons.util;

import junit.framework.TestCase;

public class PooledExecutorTest extends TestCase implements IPoolObserver {
	
	boolean limitTriggered = false;
	boolean timeoutTriggered = false;
	
	private final Runnable dummyRunnalbe = new Runnable() {
		@Override
		public void run() {
			// I DO ABSOLUTELY NOTHING, BECAUSE I AM LAZY!
		}
	};
	
	private final Runnable normalRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};
	
	private final Runnable longRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};
	
	
	public void setup() {
		limitTriggered = false;
		timeoutTriggered = false;
	}
	
	public void testReachLimit() {
		int testLimit = 2;
		int testPoolSize = 5;
		PooledExecutor pool = new PooledExecutor(testPoolSize, testLimit, 100, this);
		
		
		int max = testLimit + testPoolSize + 2;
		for (int i=0; i<max; i++) {
			pool.execute(normalRunnable, 100, null);
			if (i < max - 2) {
				// only use the one with string-concatenation if it failed to see details
				//assertFalse("limit should not be reached at " + i + "/" + (max-1), limitTriggered);
				assertFalse(limitTriggered);
			}
		}
		
		assertTrue(limitTriggered);
	}
	
	public void testTimeoutTriggered() {
		PooledExecutor pool = new PooledExecutor(5, 2, 100, this);
		
		pool.execute(longRunnable, 100, new ITimeout() {
			@Override
			public void timedOut() {
				timeoutTriggered = true;
			}
		});
		Util.sleep(1500);
		assertTrue(timeoutTriggered);
		assertFalse(limitTriggered);
	}
	
	@Override
	public void onQueueLimitReached(int size) {
		limitTriggered = true;
	}

}
