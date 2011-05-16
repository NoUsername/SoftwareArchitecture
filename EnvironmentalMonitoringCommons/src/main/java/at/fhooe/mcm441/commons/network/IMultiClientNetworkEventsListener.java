package at.fhooe.mcm441.commons.network;

public interface IMultiClientNetworkEventsListener {
	public void onNewClient(Client c, NetworkServiceClient sc);
	public void onClientDisconnectes(Client c);
}