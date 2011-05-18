package at.fhooe.mcm441.commons.util;

public interface IPoolObserver {
	void onQueueLimitReached(int currentSize);
}
