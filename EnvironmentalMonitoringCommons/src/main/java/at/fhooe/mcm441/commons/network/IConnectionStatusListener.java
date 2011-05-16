package at.fhooe.mcm441.commons.network;

public interface IConnectionStatusListener {
	public void onConnectionEstablished();
	public void onConnectionLost();
}
