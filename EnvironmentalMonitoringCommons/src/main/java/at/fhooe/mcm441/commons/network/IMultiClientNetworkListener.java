package at.fhooe.mcm441.commons.network;

public interface IMultiClientNetworkListener {
	public void onNewPackage(Client from, String newPackage);
}