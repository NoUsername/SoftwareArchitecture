package at.fhooe.mcm441.commons.protocol;

import at.fhooe.mcm441.commons.network.Client;
import at.fhooe.mcm441.commons.network.IMultiClientNetworkEventsListener;
import at.fhooe.mcm441.commons.network.IMultiClientNetworkListener;
import at.fhooe.mcm441.commons.network.MultiClientNetworkService;
import at.fhooe.mcm441.commons.network.NetworkServiceClient;

public class DummyMultiClientServer implements IMultiClientNetworkListener, IMultiClientNetworkEventsListener {
	
	public static void main(String[] args) {
		new DummyMultiClientServer(4444);
	}
	
	public DummyMultiClientServer(int port) {
		MultiClientNetworkService server = new MultiClientNetworkService(this, this);
		server.startListening(port);
	}

	@Override
	public void onNewClient(Client c, NetworkServiceClient sc) {
		System.out.println("new client! " + c.m_id + " " + c.m_address);
		sc.sendMessage("hello!");
	}

	@Override
	public void onClientDisconnectes(Client c) {
		System.out.println("client gone! " + c.m_id);
	}

	@Override
	public void onNewPackage(Client from, String newPackage) {
		System.out.println("client " + from.m_id + " said: " + newPackage);
	}

}
