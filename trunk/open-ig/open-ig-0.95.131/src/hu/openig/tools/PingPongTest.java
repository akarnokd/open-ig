/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.core.Pred1;
import hu.openig.multiplayer.MultiplayerSession;
import hu.openig.multiplayer.RemoteGameClient;
import hu.openig.multiplayer.RemoteGameListener;
import hu.openig.net.MessageClient;
import hu.openig.net.MessageConnection;
import hu.openig.net.MessageListener;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Test the ping-pong network call through connection(s).
 * @author akarnokd, 2013.04.26.
 */
public final class PingPongTest {
	/** The test program. */
	private PingPongTest() { }
	/**
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {

		ExecutorService exec = Executors.newCachedThreadPool();
		try {
			final MessageListener mlistener2 = new MessageListener(InetAddress.getLoopbackAddress(), 10002, exec);
			mlistener2.setOnConnection(new Pred1<MessageConnection>() {
				@Override
				public Boolean invoke(MessageConnection value) {
					value.setOnMessage(new RemoteGameListener(new MultiplayerSession(null, null)));
					return true;
				}
			});
			mlistener2.connect();
			try {
				final MessageClient mc = new MessageClient(InetAddress.getLoopbackAddress(), 10002);
				mc.connect();

				
				final MessageListener mlistener1 = new MessageListener(InetAddress.getLoopbackAddress(), 10001, exec);
		
				mlistener1.setOnConnection(new Pred1<MessageConnection>() {
					@Override
					public Boolean invoke(MessageConnection value) {
						value.setOnMessage(
								new RemoteGameListener(new RemoteGameClient(mc), false));
						return true;
					}
				});
				mlistener1.connect();
		
				try {
					MessageClient mclient2 = new MessageClient(InetAddress.getLoopbackAddress(), 10002);
					final RemoteGameClient client2 = new RemoteGameClient(mclient2);
					
					mclient2.connect();
					System.out.println("Client 2 Test");
					try {
						for (int j = 0; j < 10; j++) {
							System.out.println(client2.ping());
						}
					} finally {
						mclient2.close();
					}
			
					System.out.println("Client 1 Test");
					MessageClient mclient1 = new MessageClient(InetAddress.getLoopbackAddress(), 10001);
					final RemoteGameClient client1 = new RemoteGameClient(mclient1);
					mclient1.connect();
					try {
						for (int j = 0; j < 10; j++) {
							System.out.println(client1.ping());
						}
					} finally {
						mclient1.close();
					}
				} finally {
					mlistener1.close();
				}
			} finally {			
				mlistener2.close();
			}
		} finally {
			exec.shutdown();
		}
	}

}
