package at.fhooe.mcm441.commons.protocol;

/**
 * this interface can be used if you want to expose only some data-sink over which
 * you can send data
 * @author Paul Klingelhuber
 */
public interface IMessageSender {
	
	/**
	 * send some text
	 * @param msg
	 * @return 
	 */
	public boolean sendMessage(String msg);
}
