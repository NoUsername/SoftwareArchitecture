package at.fhooe.mcm441.Monitoring.network;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetAddress;
import java.net.Socket;
import java.security.InvalidParameterException;

import org.slf4j.Logger;

public class NetworkServiceClient {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());

	protected PackageListener update;
	protected boolean isRunning = false;
	protected Thread thread = null;
	protected Socket curSock = null;
	
	public NetworkServiceClient(PackageListener update) {
		this.update = update;
	}
	
	public void stop() {
		isRunning = false;
		try {
			Thread.sleep(1000);
			if (thread.isAlive()) {
				thread.stop();
				curSock.close();
			}
		} catch (Exception e) {
			log.warn("error while trying to stop.... ", e);
		}
	}
	
	public boolean isRunning() {
		return isRunning || (thread != null && thread.isAlive());
	}
	
	public boolean sendMessage(String msg) {
		if (!isRunning || curSock == null)
			return false;
		
		try {
			OutputStream os = curSock.getOutputStream();
			int len = msg.length();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeLong((long)len);
			oos.flush();
			os.write(msg.getBytes());
			os.flush();
			return true;
		} catch (Exception e) {
			log.warn("error while sending", e);
			return false;
		}
	}

	public boolean connectAndStart(InetAddress addr, int port) {
		if (isRunning)
			return false;
		
		try {
			isRunning = true;
			curSock = new Socket(addr, port);
			thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					clientLoop();
				}
			});
			thread.start();
			return true;
		} catch (Exception e) {
			log.warn("cannot start client socket ", e);
			isRunning = false;
			return false;
		}
	}
	
	private void clientLoop() {
		InputStream is = null;
		try {
			is = curSock.getInputStream();
			Reader r = new InputStreamReader( is );
			
			do {
				long l = readPackageSize(is);
				
				String msg = readText(r, l);
				update.onNewPackage(msg);
				
			} while (isRunning);
		} catch (Exception _e) {
			log.warn("problem with client");
			tryClose(is);
			isRunning = false;
		}
	}
	
	/* **
	 * HELPER METHODS
	 */
	
	protected long readPackageSize(InputStream is) throws IOException {
		return new ObjectInputStream(is).readLong();			
	}
	
	protected String readText(Reader r, long numOfChars) throws IOException {
		if (numOfChars > Integer.MAX_VALUE)
			throw new InvalidParameterException("huge texts not supported yet");
		
		StringBuffer sb = new StringBuffer((int)numOfChars);
		int read = 0;
		while (read < numOfChars) {
			char[] buffer = new char[(int)numOfChars - read];
			int count = r.read(buffer);
			sb.append(buffer, 0, count);
			read += count;
		}
		return sb.toString();
	}
	
	
	/* **
	 * utility methods
	 */
	
	public static void tryClose(Closeable c) {
		if (c ==null)
			return;
		
		try {
			c.close();
		} catch (Exception e) {
			// ignored here
		}
	}
}