/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen;

import hu.openig.core.Func1;
import hu.openig.model.GameEnvironment;
import hu.openig.model.MultiplayerDefinition;
import hu.openig.model.RemoteGameAPI;
import hu.openig.model.RemoteGameAsyncAPI;
import hu.openig.multiplayer.MultiplayerSession;
import hu.openig.multiplayer.RemoteGameAsyncClient;
import hu.openig.multiplayer.RemoteGameClient;
import hu.openig.multiplayer.RemoteGameListener;
import hu.openig.net.MessageClient;
import hu.openig.net.MessageConnection;
import hu.openig.net.MessageListener;
import hu.openig.net.UPnPGatewayDevice;
import hu.openig.net.UPnPGatewayDiscover;
import hu.openig.net.UPnPPortMappingEntry;
import hu.openig.utils.Exceptions;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * @author akarnokd, 2013.05.18.
 *
 */
public class MultiplayerContext {
	/** The network pool. */
	public ExecutorService netPool;
	/** The active multiplayer definition. */
	public MultiplayerDefinition definition;
	/** The multiplayer server. */
	public RemoteGameListener listener;
	/** The multiplayer server. */
	public MessageListener server;
	/** Is the UPNP enabled? */
	public boolean upnp;
	/** The server port number. */
	public int serverPort;
	/** The server address. */
	public InetAddress serverAddress;
	/** External IP address if UPNP is setup. */
	public String externalIPAddress;
	/** The active gateway. */
	protected UPnPGatewayDevice activeGateway;
	/** The commons accessor API. */
	protected final GameEnvironment env;
	/** The messaging client. */
	protected MessageClient client;
	/** The remote game API. */
	public RemoteGameAPI remoteAPI;
	/** The remote asynchronous API. */
	public RemoteGameAsyncAPI remoteAsyncAPI;
	/** The remote session id after the login. */
	public String remoteSessionId;
	/**
	 * Costructor, stores the game environment.
	 * @param env the environment
	 */
	public MultiplayerContext(GameEnvironment env) {
		this.env = env;
	}
	/**
	 * Start a server on the target port and
	 * map it to UPNP if necessary.
	 * @param serverAddress the server address
	 * @param serverPort the server port
	 * @param upnp use UPNP?
	 */
	public void startServer(InetAddress serverAddress, int serverPort, boolean upnp) {
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.upnp = upnp;

		netPool = Executors.newCachedThreadPool();

		server = new MessageListener(serverAddress, serverPort, netPool);
		server.setOnConnection(new Func1<MessageConnection, Boolean>() { 
			@Override
			public Boolean invoke(MessageConnection value) {
				value.setOnMessage(new RemoteGameListener(new MultiplayerSession(definition, env), true));
				return true;
			}
		});
		server.connect();
		if (upnp) {
			setupUPNP();
		}
	}
	/**
	 * Setup an UPNP port forwarding with the same serverPort to the outside.
	 */
	protected void setupUPNP() {
		try {
			UPnPGatewayDiscover gatewayDiscover = new UPnPGatewayDiscover();
			Map<InetAddress, UPnPGatewayDevice> gateways = gatewayDiscover.discover();
			if (gateways.isEmpty()) {
				throw new IOException("Unable to find UPNP gateway devices.");
			}
			activeGateway = gatewayDiscover.getValidGateway();
			if (activeGateway == null) {
				throw new IOException("Unable to find Active UPNP gateway device.");
			}
			externalIPAddress = activeGateway.getExternalIPAddress();
			UPnPPortMappingEntry portMapping = new UPnPPortMappingEntry();
			if (activeGateway.getSpecificPortMappingEntry(serverPort, "TCP", portMapping)) {
				throw new IOException("Port " + serverPort + " in use on the gateway device.");
			}
			// test static lease duration mapping
			if (!activeGateway.addPortMapping(serverPort, serverPort, serverAddress.getHostAddress(), "TCP", "test")) {
				throw new IOException("Port " + serverPort + " on " + serverAddress + " couldn't be mapped on the gateway");
			}		
		} catch (IOException | SAXException | ParserConfigurationException ex) {
			externalIPAddress = null;
			activeGateway = null;
			Exceptions.add(ex);
		}
	}
	/**
	 * Rear down any UPNP setup.
	 */
	protected void teardownUPNP() {
		if (activeGateway != null) {
			try {
				if (!activeGateway.deletePortMapping(serverPort, "TCP")) {
					throw new IOException("Unable to delete port mapping ");
				}
			} catch (Exception ex) {
				Exceptions.add(ex);
			}
			activeGateway = null;
			externalIPAddress = null;
		}
	}
	/**
	 * Stop the running server.
	 */
	public void stopServer() {
		if (server != null) {
			if (upnp) {
				teardownUPNP();
			}
			try {
				server.close();
			} catch (IOException ex) {
				Exceptions.add(ex);
			}
			netPool.shutdown();
			netPool = null;
			server = null;
			serverAddress = null;
			serverPort = -1;
			upnp = false;
		}
	}
	/**
	 * Establish a connection to the remote client.
	 * @param remoteAddress the remote address
	 * @param remotePort the remote port
	 * @throws IOException if an error occurs
	 */
	public void startClient(InetAddress remoteAddress, int remotePort) throws IOException {
		MessageClient client = new MessageClient(remoteAddress, remotePort);
		
		RemoteGameClient remoteAPI = new RemoteGameClient(client);
		RemoteGameAsyncClient remoteAsyncAPI = new RemoteGameAsyncClient(client);
		
		client.connect();
		
		this.client = client;
		this.remoteSessionId = null;
		this.remoteAPI = remoteAPI;
		this.remoteAsyncAPI = remoteAsyncAPI;
	}
	/**
	 * Stop the remote client.
	 */
	public void stopClient() {
		if (client != null) {
			try {
				client.close();
			} catch (IOException ex) {
				Exceptions.add(ex);
			}
			remoteAPI = null;
			remoteAsyncAPI = null;
			remoteSessionId = null;
		}
	}
}
