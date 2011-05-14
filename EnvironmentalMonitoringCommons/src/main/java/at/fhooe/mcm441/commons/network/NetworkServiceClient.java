package at.fhooe.mcm441.commons.network;

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

import at.fhooe.mcm441.commons.protocol.IMessageSender;


public class NetworkServiceClient implements IMessageSender {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());

	protected IPackageListener m_update;
	protected boolean m_isRunning = false;
	protected Thread m_thread = null;
	protected Socket m_curSock = null;
	protected IConnectionStatusListener m_conStatusListener = null;
	
	public NetworkServiceClient(IPackageListener update) {
		this.m_update = update;
	}
	
	public NetworkServiceClient(IPackageListener update, IConnectionStatusListener statusListener) {
		this(update);
		m_conStatusListener = statusListener;
	}
	
	/**
	 *
	 * this constructor is package-private by design! don't make it public/private!
	 *
	 */
	NetworkServiceClient(Socket curSock, IPackageListener listener, IConnectionStatusListener statusListener) {
		this.m_curSock = curSock;
		this.m_update = listener;
		m_conStatusListener = statusListener;
	}
	
	public void stop() {
		m_isRunning = false;
		try {
			Thread.sleep(1000);
			if (m_thread.isAlive()) {
				m_thread.stop();
				m_curSock.close();
			}
		} catch (Exception e) {
			log.warn("error while trying to stop.... ", e);
		}
	}
	
	public boolean isRunning() {
		return m_isRunning || (m_thread != null && m_thread.isAlive());
	}
	
	public boolean sendMessage(String msg) {
		if (!m_isRunning || m_curSock == null)
			return false;
		
		try {
			OutputStream os = m_curSock.getOutputStream();
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
		if (m_isRunning)
			return false;
		
		try {
			m_isRunning = true;
			m_curSock = new Socket(addr, port);
			m_thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					clientLoop();
				}
			});
			m_thread.start();
			if (m_conStatusListener != null)
				m_conStatusListener.onConnectionEstablished();
			return true;
		} catch (Exception e) {
			log.warn("cannot start client socket ", e);
			m_isRunning = false;
			if (m_conStatusListener != null)
				m_conStatusListener.onConnectionLost();
			return false;
		}
	}
	
	/**
	 * leave this method package-privat!
	 * @return
	 */
	boolean connectToCurrentSocket() {
		if (m_isRunning)
			return false;
		
		try {
			m_isRunning = true;
			m_thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					clientLoop();
				}
			});
			m_thread.start();
			if (m_conStatusListener != null)
				m_conStatusListener.onConnectionEstablished();
			return true;
		} catch (Exception e) {
			log.warn("cannot start client socket ", e);
			m_isRunning = false;
			if (m_conStatusListener != null)
				m_conStatusListener.onConnectionLost();
			return false;
		}
	}
	
	private void clientLoop() {
		InputStream is = null;
		try {
			is = m_curSock.getInputStream();
			Reader r = new InputStreamReader( is );
			
			do {
				long l = readPackageSize(is);
				
				String msg = readText(r, l);
				m_update.onNewPackage(msg);
				
			} while (m_isRunning);
		} catch (Exception _e) {
			log.warn("problem with client");
			tryClose(is);
			m_isRunning = false;
		}
		
		if (m_conStatusListener != null)
			m_conStatusListener.onConnectionLost();
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