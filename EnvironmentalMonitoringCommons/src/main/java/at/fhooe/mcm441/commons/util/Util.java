package at.fhooe.mcm441.commons.util;

public class Util {
	private Util() {
	}
	
	/**
	 * simple sleep without the exceptions etc
	 * @param milliseconds
	 */
	public static final void sleep(long milliseconds) {
		long now = System.currentTimeMillis();
		long sleepUntil = now + milliseconds;
		while (now < sleepUntil) {
			now = System.currentTimeMillis();
			try {
				long sTime = sleepUntil - now;
				if (sTime > 0) {
					Thread.sleep(sTime);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * simple sleep without the exceptions etc, if interrupted
	 * it will simply return, no more sleeps will be attempted
	 * @param milliseconds
	 */
	public static final void trySleep(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
		}
	}

}
