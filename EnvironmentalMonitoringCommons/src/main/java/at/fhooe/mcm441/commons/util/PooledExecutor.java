package at.fhooe.mcm441.commons.util;

import java.security.InvalidParameterException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * an improved threadpool that supports timeouts and queue-limits
 * 
 * @author Paul Klingelhuber
 */
public class PooledExecutor implements IFinished {
	
	/** size of the threadpool */
	private int m_size = 1;
	/** at which size to trigger a warning */
	private int m_queueSizeWarning = 1;
	/** how long one task is maximally allowed to take */
	private int m_maxExecutionTime = 1;
	/** counts how many threads there are currently that are executing */
	private AtomicInteger m_currentlyExecuting = new AtomicInteger(0);
	/** the actual thread-pool that takes care of running our tasks */
	private ExecutorService m_execService;
	/** inform that guy when the queue gets too long */
	private IPoolObserver m_observer;
	/** used to check for timeouts, currently we only use one which
	 * could be problematic when having many timeouts, but it helps us save
	 * threads
	 */
	private Timer timeoutTimer;
	
	
	public PooledExecutor(int size, int waitQueueWarningLimit, int maxOverallResponseTime, IPoolObserver callback) {
		m_size = size;
		m_queueSizeWarning = waitQueueWarningLimit;
		m_maxExecutionTime = maxOverallResponseTime;
		m_observer = callback;
		m_execService = Executors.newFixedThreadPool(m_size);
		timeoutTimer = new Timer();
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
				TimerTask onTimeout = null;
				if (callback != null) {
					onTimeout = new TimerTask() {
						@Override
						public void run() {
							callback.timedOut();
						}
					};
					timeoutTimer.schedule(onTimeout, timeout);
				}
				r.run();
				PooledExecutor.this.finished();
				if (onTimeout != null) {
					onTimeout.cancel();
				}
			}
		};
		
		m_execService.execute(wrapped);
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
