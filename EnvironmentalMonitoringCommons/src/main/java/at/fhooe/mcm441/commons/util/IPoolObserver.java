package at.fhooe.mcm441.commons.util;

/**
 * implement this if you want to get callbacks about task-queues which get too big
 *  
 * @author Paul Klingelhuber
 */
public interface IPoolObserver {
	/**
	 * this will be called when the queue size gets larget than the set limit
	 * 
	 * @param currentSize the current queue-size 
	 */
	void onQueueLimitReached(int currentSize);
}
