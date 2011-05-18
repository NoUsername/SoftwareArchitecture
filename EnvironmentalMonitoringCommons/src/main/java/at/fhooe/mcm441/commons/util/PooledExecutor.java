package at.fhooe.mcm441.commons.util;

import java.security.InvalidParameterException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class PooledExecutor implements IFinished {
	
	/** size of the threadpool */
	private int m_size = 1;
	/** at which size to trigger a warning */
	private int m_queueSizeWarning = 1;
	/** how long one task is maximally allowed to take */
	private int m_maxExecutionTime = 1;
	
	private AtomicInteger m_currentlyExecuting = new AtomicInteger(0);
	
	private ExecutorService execService;
	
	private IPoolObserver m_observer;
	
	
	public PooledExecutor(int size, int waitQueueWarningLimit, int maxOverallResponseTime, IPoolObserver callback) {
		m_size = size;
		m_queueSizeWarning = waitQueueWarningLimit;
		m_maxExecutionTime = maxOverallResponseTime;
		m_observer = callback;
		execService = Executors.newFixedThreadPool(m_size);
	}
	
	/**
	 * 
	 * @param r
	 * @param maxExecTime use -1 to set it to the globally set one, if you supply a value larger than the global max, an exception will be thrown
	 * @param callback this will be called if the timeout is reached
	 */
	public void execute(final Runnable r, int maxExecTime, final ITimeout callback) {
		
		if (maxExecTime == -1) {
			maxExecTime = m_maxExecutionTime;
		} else if (maxExecTime > m_maxExecutionTime) {
			throw new InvalidParameterException("max exec time of " + maxExecTime + " invalid, because you set a global limit of " + m_maxExecutionTime);
		}
		
		int waitQueueSize = m_currentlyExecuting.get() - m_size;
		if (waitQueueSize > m_queueSizeWarning) {
			m_observer.onQueueLimitReached(waitQueueSize);
		}
		
		final int timeout = maxExecTime;
		
		Runnable wrapped = new Runnable() {
			public void run() {
				Timer t = new Timer();
				if (callback != null) {
					t.schedule(new TimerTask() {
						@Override
						public void run() {
							callback.timedOut();
						}
					}, timeout);
				}
				r.run();
				PooledExecutor.this.finished();
				t.cancel();
			}
		};
		
		execService.execute(wrapped);
		m_currentlyExecuting.incrementAndGet();
	}

	@Override
	public void finished() {
		m_currentlyExecuting.decrementAndGet();
	}

}

interface IFinished {
	public void finished();
}
