package at.fhooe.mcm441.commons.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class Util {
	private Util() {
	}

	/**
	 * simple sleep without the exceptions etc
	 * 
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
	 * simple sleep without the exceptions etc, if interrupted it will simply
	 * return, no more sleeps will be attempted
	 * 
	 * @param milliseconds
	 */
	public static final void trySleep(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * copy one file to the different location
	 * 
	 * @param in
	 *            the file you want to copy
	 * @param out
	 *            the new destination where you want to copy
	 * @throws IOException
	 */
	public static void copyFile(File in, File out) throws IOException {
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (IOException e) {
			throw e;
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}

}
