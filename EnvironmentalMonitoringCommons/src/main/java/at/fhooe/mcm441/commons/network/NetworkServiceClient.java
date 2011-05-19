package at.fhooe.mcm441.commons.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
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
			Thread.sleep(50);
			if (m_thread != null && m_thread.isAlive()) {
				m_thread.stop();
			}
			NWUtils.tryClose(m_curSock);
		} catch (Exception e) {
			log.warn("error while trying to stop.... ", e);
		}
	}
	
	public boolean isRunning() {
		return m_isRunning || (m_thread != null && m_thread.isAlive());
	}
	
	public synchronized boolean sendMessage(String msg) {
		if (!m_isRunning || m_curSock == null) {
			// this log message can clutter up the server console
			//log.warn("cannot send sth on a non-running or socketless client");
			return false;
		}
		
		ByteBuffer lenBuf = ByteBuffer.wrap(new byte[8]);
		try {
			OutputStream os = m_curSock.getOutputStream();
			byte[] data = msg.getBytes();
			int len = data.length;
			lenBuf.rewind();
			lenBuf.putLong(len);
			os.write(lenBuf.array());
			os.write(data);
			os.flush();
			return true;
		} catch (Exception e) {
			//log.warn("error while sending", e);
			return false;
		}
	}

	public boolean connectAndStart(InetAddress addr, int port) {
		if (m_isRunning) {
			log.warn("tried to start an already running client");
			return false;
		}
		
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
	
	public void setConnectionStatusListener(IConnectionStatusListener conListener) {
		m_conStatusListener = conListener;
	}
	
	/**
	 * leave this method package-privat!
	 * @return
	 */
	boolean connectToCurrentSocket() {
		if (m_isRunning) {
			log.warn("tried to start already running client");
			return false;
		}
		
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
			
			do {
				long l = readPackageSize(is);
				
				String msg = readText(is, l);
				m_update.onNewPackage(msg);
				
			} while (m_isRunning);
		} catch (Exception e) {
			if (m_isRunning) {
				//log.warn("problem with client", e);
			} else {
				// an expected error
			}
			NWUtils.tryClose(is);
			m_isRunning = false;
		}
		
		if (m_conStatusListener != null)
			m_conStatusListener.onConnectionLost();
	}
	
	/* **
	 * HELPER METHODS
	 */
	
	protected long readPackageSize(InputStream is) throws IOException {
		byte[] buf = new byte[8];
		int readCount = 0;
		while (readCount < buf.length) {
			readCount += is.read(buf, readCount, buf.length - readCount);
		}
		return ByteBuffer.wrap(buf).getLong();
	}
	
	protected String readText(InputStream is, long numOfBytes) throws IOException {
		if (numOfBytes > Integer.MAX_VALUE)
			throw new InvalidParameterException("huge texts not supported yet");
		
		byte[] buf = new byte[(int)numOfBytes];
		int read = 0;
		while (read < numOfBytes) {
			read += is.read(buf, read, buf.length - read);
		}
		return new String(buf);
	}
	
}